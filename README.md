# Software Engineering- CS370

## Getting Started
The CunyFirst Registration mobile application is a mobile application that helps CUNY college students get notified when the classes they desire open up and optionally automatically register them for their class. The application allows users to select their desired classes through a simple selection interface. If the user wants to have their classes automatically registered, they will be presented with the CunyFirst login page. When they login to CunyFirst, their CunyFirst cookies will be extracted. Their selection and the extracted cookies will be sent and stored to a backend server along with an unique identifier. This data will be stored in a Firebase Firestore database.

The backend will be continuously polling the CunyFirst website for class open/closed status changes. When a desired class opens up, if the user opted out of automatic registration, the user will simply receive a push notification alerting them that one of their selected classes have opened up. Otherwise, the backend will automatically register the class for the user using the stored user cookies.

The backend will need to refresh stored user cookies every 25 minutes to keep them from expiring.

## User Interface
* The mobile application is split into three activities:

* A home/main activity that shows the user the classes they have currently selected and two buttons: one for selecting new classes and one for opting for automatic registration

* A class selection activity that allows users to select a class to add to their desired list in an intuitive way. This activity will make an ajax request to the backend to get an updated list of classes offered by CUNY schools.

* A login activity for logging into CunyFirst and extracting user cookies.

### Prerequisites
Android 7 and plus.

