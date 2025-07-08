/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");

admin.initializeApp();
sgMail.setApiKey(functions.config().sendgrid.key);

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({maxInstances: 10});

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.sendIdeaConfirmationEmail = functions.firestore
    .document("ideas/{ideaId}")
    .onCreate(async (snap, context) => {
        const idea = snap.data();
        const msg = {
            to: idea.submitterEmail,
            from: "sandorfulop44@gmail.com", // Verified sender email
            subject: "We received your idea!",
            text: `Hi ${idea.submitterName},\n\nThank you for submitting your idea: "${idea.title}".\n\nWe will review it soon!\n\nBest,\nYour App Team`, // eslint-disable-line max-len
            html: `<p>Hi ${idea.submitterName},</p>\
                <p>Thank you for submitting your idea: <strong>${idea.title}</strong>.</p>\
                <p>We will review it soon!</p>\
                <p>Best,<br>Your App Team</p>`, // eslint-disable-line max-len
        };
        try {
            await sgMail.send(msg);
            console.log("Confirmation email sent to", idea.submitterEmail);
        } catch (error) {
            console.error("Error sending email:", error);
        }
        return null;
    });
