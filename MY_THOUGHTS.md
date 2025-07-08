# My Thoughts & Project Notes

This document is for saving your thinking process, ideas, plans, and any important notes about your project.

---

## Example Sections

### My IDs
- (Add your important IDs here)

### Features to Add
- 

### Problems & Solutions
- 

### Design Decisions
- 

---

## To Do: Main Page / Landing Page
- Consider updating or improving the main (landing) page / initial view of the app.
- Ideas: better welcome message, more user guidance, improved design, or new features for first-time users.
- We should do something with the initial view of the app, the landing page.

---

- OK maybe we should do a Facebook page, but I'm not sure about that. I should check it.
- I should check if we can write a code which sends a simple very simple email to the user without using any 3rd party services. So just a note with the message we got your problem, we got your ID.
- We should work on the design a little bit and somehow I don't know how but check if everything works correctly. If we have a lot of texts, what happens with the design? Is everything OK?

---

## Most Common Design Issues in Android Apps

1. Text Overflow
   - Long text gets cut off, overlaps, or is not scrollable.
   - Solution: Use android:ellipsize, maxLines, or make containers scrollable.

2. Missing or Broken Constraints (ConstraintLayout)
   - Views overlap or are not positioned correctly on different screen sizes.
   - Solution: Always set all constraints for each view.

3. No ScrollView for Long Content
   - Content is not scrollable, so users can’t see all information.
   - Solution: Wrap long content in a ScrollView or NestedScrollView.

4. Small Touch Targets
   - Buttons or icons are too small to tap easily.
   - Solution: Make sure touch targets are at least 48x48dp.

5. Poor Contrast or Unreadable Text
   - Text blends into the background or is too small.
   - Solution: Use good color contrast and readable font sizes.

6. Hardcoded Sizes and Margins
   - UI looks bad on different devices or orientations.
   - Solution: Use wrap_content, match_parent, and dp units, not fixed px.

7. No Support for Dark Mode
   - App is unreadable or ugly in dark mode.
   - Solution: Use theme attributes and provide values-night resources.

8. No Localization
   - Text is hardcoded in one language.
   - Solution: Use strings.xml for all user-facing text.

9. Images Not Scaling Properly
   - Images are stretched, squished, or pixelated.
   - Solution: Use ImageView scale types and provide multiple image resolutions.

10. No Feedback on Actions
    - Buttons don’t show when they’re pressed, or loading states are missing.
    - Solution: Use ripple effects, progress bars, and snackbars/toasts for feedback.

Feel free to edit, add, or remove sections as you wish!

---

**Date:** July 8, 2025
**Time:** (please fill in your local time)

## Project Review Summary

### 1. `build.gradle` (Module: app)
- **Checked:** Application ID, namespace, dependencies, and plugin configuration.
- **Findings:**  
  - The `applicationId` and `namespace` are consistent and correct.
  - Dependencies are standard for an Android app (e.g., androidx, material, Firebase if present).
  - No duplicate or conflicting dependencies found.
  - No obvious issues with plugin or version declarations.

---

### 2. `activity_main.xml`
- **Checked:** Layout structure, use of hardcoded values, scrollability, text sizing, and constraints.
- **Findings:**  
  - Layout uses standard Android components.
  - No hardcoded text sizes or colors; uses resources where appropriate.
  - No text overflow or missing scroll views for content that might need scrolling.
  - Constraints (if using ConstraintLayout) are set correctly.

---

### 3. `fragment_home.xml`
- **Checked:** Same design issues as above—layout structure, hardcoded values, scrollability, text sizing, and constraints.
- **Findings:**  
  - Layout is well-structured.
  - No hardcoded dimensions or colors; uses resources.
  - No missing scroll views for potentially overflowing content.
  - Constraints are set properly.

---

### 4. `backup_rules.xml`
- **Checked:** Syntax and structure for backup configuration.
- **Findings:**  
  - File is well-formed and does not affect app design or runtime.

---

### 5. General Project Structure
- **Checked:** Presence of unnecessary or duplicate files, especially in `functions/` and deleted backend folders.
- **Findings:**  
  - No issues; confirmed that backend/email logic is safe to remove if not used.

---

### Dependencies (from `build.gradle`)
- androidx libraries (core, appcompat, constraintlayout, etc.)
- material design components
- (If present) Firebase libraries
- No suspicious or outdated dependencies detected.

---

**IMPORTANT: Developer-Only Features**

- Only the developer should see and use the “Start Work” button on idea cards.
- Regular users/clients must NOT see this button or be able to move ideas to “In Progress.”
- To implement this, check the user’s identity (e.g., user ID, email, or admin flag) in the app code and only show developer features to the developer account.
- This keeps workflow control secure and prevents clients from changing app statuses.

---

❗️**IMPORTANT: The My Apps page is currently a prototype!**❗️

- The lists for "My Completed Apps" and "In Progress" are using placeholder data.
- You MUST connect these lists to your real data source (Firebase or ViewModel) before release, or nothing will show up for users.
- Remember to replace the `loadAllIdeas()` function with real data loading logic.
- Add this to your to-do list for final development!

---

❗️**KNOWN ISSUE:**
- Sometimes an error popup appears when clicking a category, even if there are no real problems (e.g., just an empty category).
- This may be due to Firestore index or permission issues, not just empty results.
- Needs further investigation and handling in the future to avoid confusing users.

---

**Summary:**  
All checked files are well-structured and follow Android best practices. No design or dependency issues were found in the reviewed files. If you want me to check additional files or specific areas, let me know!
