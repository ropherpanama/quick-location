let functions = require('firebase-functions');
let admin = require('firebase-admin');
const GeoFire = require('geofire');

admin.initializeApp(functions.config().firebase);

const geoFire = new GeoFire(admin.database().ref('/geofire'));


exports.saveUser = functions.database.ref('/users/{pushId}')
    .onWrite(event => {
        const user = event.data.val();
        console.log("key user", '' + event.params.pushId);
        var latitude = event.data.child('latitude');
        var longitude = event.data.child('longitude');
        if (latitude.changed() || longitude.changed()) {
            const location = [user.latitude, user.longitude];
            return geoFire.set(event.params.pushId, location);
        }
    });


exports.sendPush = functions.database.ref('/messages/{chatsUid}/{chatUid}').onWrite(event => {

    const chatUid = event.data.val();
    const titleName = chatUid.userModel.name;
    const groupId = chatUid.id;
    const text = chatUid.message;

    console.log('Grupo ID ' + groupId);
    let payload = {
        data: {
            title: titleName,
            body: text
        }
    };

    findUserByGroup(groupId, payload, chatUid.userModel.name);

});


/*Busca  los Usuarios */
function findUserByGroup(id, payload, name) {
    let dbRef = admin.database().ref('/groups/' + id);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();

            console.log('Title ' + data.members);
            for (var property in data.members) {
                console.log('property ' + property);
                sendNotifyByMessage(property, payload, name);
            }

            resolve();
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}

exports.placeAdd = functions.database.ref('/places/new/data/{placeId}')
    .onWrite(event => {

        if (event.data.previous.exists()) {

            return;
        }

        if (!event.data.exists()) {

            return;
        }


        const original = event.data.val();
        const id = event.params.placeId;

        event.data.ref.remove();

        admin.database().ref('/places/data/').child(id).set(original);
        updatePlaceByReview(id);
        updatePlaceByReport(id);


    });


exports.reviewAdd = functions.database.ref('/places/new/reviews/{placeId}/{pushId}/')
    .onWrite(event => {

        if (event.data.previous.exists()) {

            return;
        }

        if (!event.data.exists()) {

            return;
        }


        const original = event.data.val();
        var eventSnapshot = event.data;
        var review = {
            authorName: "",
            comment: "",
            date: "",
            rating: 0
        };
        const id = event.params.placeId;
        review.authorName = original.author;
        review.date = admin.database.ServerValue.TIMESTAMP;
        for (i = 0; i < original.informations.length; i++) {
            if (original.informations[i].informationTag == 'rating') {
                review.rating = Number(original.informations[i].informationContent);
            }
            if (original.informations[i].informationTag == 'comment') {
                review.comment = original.informations[i].informationContent;
            }
        }



        event.data.ref.remove();

        admin.database().ref('/places/reviews/').child(id).push(review);

        return updatePlaceByReview(id);

    });

/*Actualiza los datos del place segun su review*/
function updatePlaceByReview(id) {
    let dbRef = admin.database().ref('/places/reviews/' + id);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();

            var size = 0;
            var raitingTotal = 0;
            for (var property in data) {


                raitingTotal += data[property].rating;
                size++;
            }
            if (size > 0) {



                admin.database().ref('/places/data/').child(id).update({
                    "rating": (raitingTotal / size),
                    reviewsCount: size
                });


            } else {
                admin.database().ref('/places/data/').child(id).update({
                    "rating": 0,
                    reviewsCount: 0
                });
            }

            resolve();
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}



exports.reportAdd = functions.database.ref('/places/new/report-issue/{placeId}/{pushId}/')
    .onWrite(event => {

        if (event.data.previous.exists()) {

            return;
        }

        if (!event.data.exists()) {

            return;
        }


        const original = event.data.val();
        var eventSnapshot = event.data;
        var report = {
            author: "",
            field: "",
            date: "",
            field_human: "",
            value: ""
        };
        const id = event.params.placeId;
        report.date = admin.database.ServerValue.TIMESTAMP;
        report.author = original.author;

        for (i = 0; i < original.informations.length; i++) {
            if (original.informations[i].informationTag == 'location') {
                report.field = original.informations[i].informationTag;
                report.field_human = 'Locacion'
                var res = original.informations[i].informationContent.split(":");

                var geometry = {
                    location: {
                        lat: 0,
                        lng: 0
                    }
                }

                geometry.location.lat = Number(res[0]);
                geometry.location.lng = Number(res[1]);
                report.value = geometry;
            }
            if (original.informations[i].informationTag == 'address') {
                report.field = 'formattedAddress';
                report.field_human = 'Direccion'
                report.value = original.informations[i].informationContent;
            }
            if (original.informations[i].informationTag == 'telephone') {
                report.field = 'formattedPhoneNumber';
                report.field_human = 'Telefono'
                report.value = original.informations[i].informationContent;
            }
            if (original.informations[i].informationTag == 'name') {
                report.field = original.informations[i].informationTag;;
                report.field_human = 'Nombre'
                report.value = original.informations[i].informationContent;
            }
            if (original.informations[i].informationTag == 'schedule') {
                report.field = 'openingHours';
                report.field_human = 'Horarios'
                var schArrary = original.informations[i].schedules;
                var text = "";
                var dataDate = {
                    weekdayText: []
                }
                let dataArray = [];
                for (i = 0; i < schArrary.length; i++) {
                    text += schArrary[i].dayName + ":" + schArrary[i].openFrom + " - " + schArrary[i].closedFrom + ","
                    dataArray.push(schArrary[i].dayName + ":" + schArrary[i].openFrom + " - " + schArrary[i].closedFrom);
                }
                dataDate.weekdayText = dataArray;
                report.value = dataDate;
            }

            admin.database().ref('/places/report-issue/').child(id).push(report);

        }

        event.data.ref.remove();



        return updatePlaceByReport(id);

    });

/*Actualiza los datos del place segun su review*/
function updatePlaceByReport(id) {
    let dbRef = admin.database().ref('/places/report-issue/' + id);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();

            var size = 0;
            for (var property in data) {
                size++;
            }

            admin.database().ref('/places/data/').child(id).update({
                updatesCount: size
            });

            resolve();
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}

exports.reportUpdate = functions.database.ref('/places/report-issue/{placeId}/{pushId}/')
    .onWrite(event => {


        const id = event.params.pushId;
        const placeId = event.params.placeId;
        const original = event.data.val();
        if (event.data.exists() && event.data.previous.exists()) {
            if (original.hasOwnProperty("done")) {
                if (original.done) {
                    event.data.ref.remove();
                }
            }
            if (original.hasOwnProperty("remove")) {
                if (original.remove) {
                    event.data.ref.remove();
                }
            }

        } else if (!event.data.exists() && event.data.previous.exists()) {
            event.data.ref.remove();
            return updatePlaceByReport(placeId);
        }
        return updatePlaceByReport(placeId);
    });

exports.reviewUpdate = functions.database.ref('/places/reviews/{placeId}/{pushId}/')
    .onWrite(event => {

        const placeId = event.params.placeId;

        if (!event.data.exists() && event.data.previous.exists()) {
            event.data.ref.remove();
            return updatePlaceByReview(placeId);
        }
        return updatePlaceByReview(placeId);
    });



exports.findPos = functions.database.ref('/groups/{groupId}')
    .onWrite(event => {

        if (event.data.previous.exists()) {

            return;
        }

        if (!event.data.exists()) {

            return;
        }

        const groupData = event.data.val();
        var lat = Number(groupData.latitude);
        var lon = Number(groupData.longitude);
        var radius = Number(5);
        var id = event.params.groupId;
        var createBy = groupData.create_by;
        const titleName = groupData.title;
        const text = groupData.description;
        console.log('Hay cambios en ' + id);
        console.log('Data latitud del Grupo' + lat);
        console.log('Data Longitud del Grupo' + lon);

        var geoQuery = geoFire.query({
            center: [lat, lon],
            radius: radius
        });

        var keys = [];
        geoQuery.on("key_entered", function(key, location, distance) {
            console.log(key + " is located at [" + location + "] which is within the query (" + distance.toFixed(2) + " km from center)");
            keys.push(key);
        });


        console.log('Distance' + keys);


        var payload = {
            data: {
                title: titleName,
                body: text
            }
        };
        for (var userKey of keys) {
            console.log('userKey' + userKey);
            admin.database().ref('/groups').child(id).child("members").child(userKey).update({
                value: true
            });
            if (userKey !== createBy)
                sendNotify(userKey, payload);
        }

    });


function sendNotify(id, payload) {
    let dbRef = admin.database().ref('/users').child(id);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            console.log('data User token_fcm' + data.token_fcm);

            admin.messaging().sendToDevice(data.token_fcm, payload);

            resolve();
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}


function sendNotifyByMessage(id, payload, name) {
    let dbRef = admin.database().ref('/users').child(id);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            console.log('data User token_fcm' + data.token_fcm);
            if (data.fullname !== name)
                admin.messaging().sendToDevice(data.token_fcm, payload);

            resolve();
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}
