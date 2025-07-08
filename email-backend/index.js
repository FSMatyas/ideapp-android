// SendGrid email relay backend for IdeaApps
// 1. Install dependencies: npm install express axios cors dotenv @sendgrid/mail
// 2. Create a .env file with your SendGrid API key
// 3. Start with: node index.js

const express = require('express');
const cors = require('cors');
const sgMail = require('@sendgrid/mail');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

sgMail.setApiKey(process.env.SENDGRID_API_KEY);

app.post('/send-email', async (req, res) => {
  const { user_name, user_email, idea_title, idea_description, submission_date } = req.body;
  const msg = {
    to: user_email,
    from: process.env.SENDGRID_FROM, // Your verified sender email
    subject: `Your App Idea "${idea_title}" Has Been Submitted!`,
    text: `Hi ${user_name},\n\nThank you for submitting your app idea to IdeaApps!\n\nYour idea details:\nTitle: ${idea_title}\nDescription: ${idea_description}\nSubmitted on: ${submission_date}\n\nWe've received your submission and it's now under review. You'll be notified once it's approved and available in the app.\n\nBest regards,\nThe IdeaApps Team`
  };
  try {
    await sgMail.send(msg);
    res.status(200).json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, error: error.response?.body || error.message });
  }
});

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`SendGrid email backend running on port ${PORT}`);
});
