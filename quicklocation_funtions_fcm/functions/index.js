let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

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
            tokens.push(user.token_fcm);
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
          tokens.push(user.token_fcm);
      }
      let payload = {
          data: {
              title: 'Nuevo grupo ' + titleName,
              body: text
             }
      };
      return admin.messaging().sendToDevice(tokens, payload);
  });
});




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
