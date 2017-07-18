let functions = require('firebase-functions');
let admin = require('firebase-admin');
const GeoFire = require('geofire');

admin.initializeApp(functions.config().firebase);
const geoFire = new GeoFire(admin.database().ref('/geofire'));

exports.sendPush = functions.database.ref('/messages/{chatsUid}/{chatUid}').onWrite(event => {
  const chatUid = event.data.val();
    let projectStateChanged = false;
    let projectCreated = false;

    let projectData = event.data.val();
    if (!event.data.previous.exists()) {
        projectCreated = true;
    }
    if (!projectCreated && event.data.changed()) {
        projectStateChanged = true;
    }

    const titleName = chatUid.userModel.name;
    const text = chatUid.message;
    //const user = countRef;
    return loadUsers().then(users => {
        let tokens =  []
        for (let user of users) {
          if (titleName != user.fullname) {
            tokens.push(user.token_fcm);
          }
        }
        let payload = {
            data: {
                title: titleName,
                body: text
               }
        };
        return admin.messaging().sendToDevice(tokens, payload);
    });
});

exports.sendNewGruop  = functions.database.ref('/groups/{groupsUID}').onWrite(event => {
  const grupo = event.data.val();
  console.log('Grupod',grupo);


  const titleName = grupo.title;
  const text = grupo.description;

  //const user = countRef;
  return loadUsers().then(users => {
      let tokens =  []
      for (let user of users) {
        console.log("key ",grupo.create_by+" ---- "+user.key);
        //console.log("user ",user);
        if (grupo.create_by != user.key) {
          tokens.push(user.token_fcm);
        }
            }
      let payload = {
          data: {
              title: 'Nuevo grupo: ' + titleName,
              body: text
             }
      };
      return admin.messaging().sendToDevice(tokens, payload);
  });
});

/*Lista a todos los usuarios con detalles del token fcm*/
function loadUsers() {
    let dbRef = admin.database().ref('/users');
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();

            let users = [];
            for (var property in data) {
                users.push(data[property]);
            }
            resolve(users);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}

exports.saveUser = functions.database.ref('/users/{pushId}')
    .onWrite(event => {
      const user = event.data.val();
      console.log("key user",''+event.params.pushId);
      var latitude = event.data.child('latitude');
      var longitude = event.data.child('longitude');
      if (latitude.changed() || longitude.changed()) {
        const location = [user.latitude, user.longitude];
        return geoFire.set(event.params.pushId, location);
      }
    });

/*exports.addNewUser = functions.https.onRequest((req, res) => {
  const user = req.body;
  var newUser = admin.database().ref('/users').push();
  newUser.set(user).then(snapshot => {
    var location = [user.latitude, user.longitude];
    geoFire.set(newUser.key, location).then(function() {
      res.send(200, "ok");
    });
  });
});*/
