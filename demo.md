# SPS — Merchant Verification, Signup & Login Flow

**Purpose:** Quick, non-technical documentation that explains how the SPS app verifies merchants, handles signup, and routes users to login.

---

## Flow diagram

```
[App Launch]
     |
[Merchant ID Verification Screen]
     |
 POST https://sportsparkingsystem.com/api/check-merchant
     | (body: { merchant_id })
     |
  +---------------------------+---------------------------+
  |                           |                           |
Exists (response: merchant   Not found (response: signup  |
data) --> Redirect to Login  link + message) --> Show     |
Screen                       dialog with button           |
                                                           |
                                  |                        |
                        User taps button -> Open WebView with
                                  signup link               |
                                  |
                       User completes signup & plan select
                                  |
                       Show message: "Open SPS app on device"
                                  |
                       User returns to app -> Merchant ID check
                                  |
                           Now exists -> Redirect to Login

After successful verify/register: create subdomain
https://{merchant}.sportsparkingsystem.com/api/
```

---

## Step-by-step (short)

1. **On app open** the Merchant ID Verification screen runs a POST to `/api/check-merchant` (full URL: `https://sportsparkingsystem.com/api/check-merchant`) with `merchant_id` from the device.
2. **If the merchant exists** the API returns merchant-related data and the app navigates the user to the **Login** screen.
3. **If the merchant does not exist** the API returns a signup website link + a message. The app shows a dialog with that message and a button.
4. **When the user taps the button** the app opens a **WebView** to the signup URL. The user completes registration and selects a plan on the website. The website then tells the user to re-open the SPS app on their device.
5. **When the user re-opens the app**, the Merchant ID verification runs again; if registration was successful the merchant will now exist and the app redirects to the **Login** screen.
6. **Subdomain creation:** After verification/registration, a subdomain is created for the merchant in the format: `https://{merchant}.sportsparkingsystem.com/api/` (e.g. `https://ankit.sportsparkingsystem.com/api/`).

---

## Plain‑language summary for non‑technical readers

When someone opens the SPS app on their device, the app checks if their merchant account already exists by asking our server for that merchant ID. If the account exists, the person goes straight to the login page. If not, the app politely tells them they need to sign up and offers a button that opens the signup page inside the app (WebView). They sign up on the website, pick a plan, and are told to come back to the app. When they re-open the app the system sees the newly created account and brings them to the login screen. We also create a personal web address (a subdomain) for every merchant once they register or are verified.

---

If you want, I can also export this as a one‑page PDF or expand it with UX screenshots and exact dialog text to show to your boss.
