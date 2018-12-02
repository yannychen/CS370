const admin = require('firebase-admin');

admin.initializeApp({
    credential: admin.credential.applicationDefault()
});
const settings = {timestampsInSnapshots: true};
admin.firestore().settings(settings);

const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;
const Timestamp = admin.firestore.Timestamp;
const Users = db.collection('users');

async function runJob() {
    const timeLimit = Timestamp.fromDate(new Date(Date.now() - (1000 * 60 * 25)));

    const users = await Users.where('lastCookieRefresh', '<=', timeLimit).get();

    if (users.empty) {
        setTimeout(runJob, 60000);
        return;
    }

    const promiseMap = users.docs.map((user) => refreshUserCookie(user));
    await Promise.all(promiseMap);

    setTimeout(runJob, 60000);
}

const defaultRequest = require('request-promise-native');
const request = defaultRequest.defaults({
    headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36'
    },
    followAllRedirects: true
});
const COOKIE_DOMAINS = ["https://home.cunyfirst.cuny.edu", "https://hrsa.cunyfirst.cuny.edu", "https://cunyfirst.cuny.edu", "https://cuny.edu"];

async function refreshUserCookie(user) {
    const userData = user.data();

    const jar = request.jar();
    for (let domain in userData.cookies) {
        const split = userData.cookies[domain].split('; ');

        split.forEach(function (cookie) {
            jar.setCookie(request.cookie(cookie), domain);
        });
    }

    const response = await request.get({
        url: 'https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSS_STUDENT_CENTER.GBL',
        jar: jar
    });

    if (response.includes('page="SSS_STUDENT_CENTER"')) {
        const updateVals = {
            lastCookieRefresh: Timestamp.now(),
            cookies: {}
        };

        for (let domain of COOKIE_DOMAINS) {
            updateVals.cookies[domain] = jar.getCookieString(domain);
        }

        user.ref.update(updateVals);
        console.log(user.id + ' cookies refreshed.');
    } else {
        console.log(response);
        user.ref.update({
            cookies: FieldValue.delete(),
            lastCookieRefresh: FieldValue.delete()
        });
        console.log(user.id + ' cookies expired.');
    }

}

runJob();