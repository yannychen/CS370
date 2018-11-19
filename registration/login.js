require('dotenv').config();

const defaultRequest = require('request-promise-native');
const j = defaultRequest.jar();
const request = defaultRequest.defaults({
    jar: j,
    headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36'
    },
    followAllRedirects: true
});
const cheerio = require('cheerio');

function getHiddenStateVals(body) {
    const $ = cheerio.load(body);
    const hiddenFields = $('#win0divPSHIDDENFIELDS > input');

    const values = {};

    hiddenFields.each(function (i, el) {
        el = $(el);
        values[el.attr('name')] = el.val();
    });

    return values;
}

async function login() {
    await request.get('https://cunyfirst.cuny.edu/');
    await request.post({
        url: 'https://ssologin.cuny.edu/oam/server/auth_cred_submit',
        form: {
            usernameH: process.env.usernameH,
            username: process.env.username_field,
            password: process.env.password,
            submit: ''
        }
    });
}

async function addToCart() {
    await login();
    a = Date.now();

    const cart = await request.get('https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL?Page=SSR_SSENRL_CART&Action=A&ACAD_CAREER=UGRD&EMPLID=23408999&INSTITUTION=QNS01&STRM=1192&TargetFrameName=None');

    let vals = getHiddenStateVals(cart);

    const addConfirm = await request.post('https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL', {
        form: Object.assign({}, vals,
            {
                ICAJAX: 1,
                ICNAVTYPEDROPDOWN: 0,
                ICAction: 'DERIVED_REGFRM1_SSR_PB_ADDTOLIST2$9$',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$: 9999,
                DERIVED_REGFRM1_CLASS_NBR: 43697,
                DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$: '06',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$: 9999,
                ptus_defaultlocalnode: 'PSFT_CNYHCPRD',
                ptus_dbname: 'CNYHCPRD',
                ptus_portal: 'EMPLOYEE',
                ptus_node: 'HRMS',
                ptus_workcenterid: '',
                ptus_componenturl: 'https://hrsa.cunyfirst.cuny.edu/psp/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL'
            }
        )
    });

    vals = getHiddenStateVals(addConfirm);

    const cart2 = await request.post('https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL', {
        form: Object.assign({}, vals,
            {
                ICAJAX: 1,
                ICNAVTYPEDROPDOWN: 0,
                ICAction: 'DERIVED_CLS_DTL_NEXT_PB$280$',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$: 9999,
                DERIVED_CLS_DTL_WAIT_LIST_OKAY$125$$chk: 'N',
                DERIVED_CLS_DTL_CLASS_PRMSN_NBR$118$: '',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$: 9999,
                ptus_defaultlocalnode: 'PSFT_CNYHCPRD',
                ptus_dbname: 'CNYHCPRD',
                ptus_portal: 'EMPLOYEE',
                ptus_node: 'HRMS',
                ptus_workcenterid: '',
                ptus_componenturl: 'https%3A%2F%2Fhrsa.cunyfirst.cuny.edu%2Fpsp%2Fcnyhcprd%2FEMPLOYEE%2FHRMS%2Fc%2FSA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL'
            }
        )
    });

    vals = getHiddenStateVals(cart2);

    return vals;
}

async function register() {
    let vals = await addToCart();

    const startEnroll = await request.post('https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL', {
        form: Object.assign({}, vals,
            {
                ICAJAX: 1,
                ICNAVTYPEDROPDOWN: 0,
                ICAction: 'DERIVED_REGFRM1_LINK_ADD_ENRL$82$',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$: 9999,
                DERIVED_REGFRM1_CLASS_NBR: '',
                DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$: '06',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$: 9999,
                ptus_defaultlocalnode: 'PSFT_CNYHCPRD',
                ptus_dbname: 'CNYHCPRD',
                ptus_portal: 'EMPLOYEE',
                ptus_node: 'HRMS',
                ptus_workcenterid: '',
                ptus_componenturl: 'https://hrsa.cunyfirst.cuny.edu/psp/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL'
            }
        )
    });

    const urlIndex = startEnroll.indexOf("<GENSCRIPT id='onloadScript'><![CDATA[document.location='");
    const midEnrollUrl = startEnroll.substring(urlIndex + "<GENSCRIPT id='onloadScript'><![CDATA[document.location='".length, startEnroll.indexOf("';]]></GENSCRIPT>", urlIndex));

    const midEnroll = await request.get(midEnrollUrl);

    vals = getHiddenStateVals(midEnroll);

    const finishEnroll = await request.post('https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_ADD.GBL', {
        form: Object.assign({}, vals,
            {
                ICAJAX: 1,
                ICNAVTYPEDROPDOWN: 0,
                ICAction: 'DERIVED_REGFRM1_SSR_PB_SUBMIT',
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$: 9999,
                DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$: 9999,
                ptus_defaultlocalnode: 'PSFT_CNYHCPRD',
                ptus_dbname: 'CNYHCPRD',
                ptus_portal: 'EMPLOYEE',
                ptus_node: 'HRMS',
                ptus_workcenterid: '',
                ptus_componenturl: 'https://hrsa.cunyfirst.cuny.edu/psp/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_ADD.GBL'
            }
        )
    });
}

let a;
register().then(function () {
    console.log(Date.now() - a);
});