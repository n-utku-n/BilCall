Group 2
Project Title: BilCall - Club and Event Management System  


Project Description:

BilCall is a desktop application developed using Java and JavaFX for managing university clubs and their events. The system allows students to view active clubs, follow them, participate in events, and add comments. Club managers can create clubs and events, manage profiles, and track participation. The backend is built using Firebase Firestore and Firebase Authentication, with Firestore used as a NoSQL database for storing all structured data including users, events, and clubs.

Current Status:
The project is mostly complete and functional. Major features such as user authentication, viewing and joining clubs and events, event filtering and sorting, image uploading, and live data synchronization through Firestore have been successfully implemented. Additionally, an admin panel has been developed, where administrators can manage user roles (e.g., change a student’s status to club manager), approve or reject club creation requests, delete events, and process applications from students who wish to become club leaders. This role-based control structure added flexibility and administrative oversight to the platform.

What Works:
Firebase Authentication (Sign-up / Sign-in)

Club creation by managers

Viewing and following clubs

Viewing events (active/expired)

Live search and sorting of events

Uploading images to Firebase Storage

Firestore integration for storing user, event, and club data

Threading and Platform.runLater() for non-blocking UI

Displaying and managing comments for events

User role detection (student vs. club manager)

Viewing all registered users and their roles

Changing user roles (e.g., promoting a student to a club manager or demoting back)

Viewing leadership applications from students (AdminLeadershipRequestsController.java)

Accepting or rejecting student requests to become club managers

Viewing all clubs with admin privileges (ClubCardAdminController.java)

Viewing and deleting events created by any club

Enforcing club creation approval before clubs are visible to users

Handling all role transitions and user moderation from a central dashboard



What Doesn’t Work or Needs Improvement:
- Some UI issues remain with dynamically updating minimum participant count in event cards.
- UI design can be improved in terms of spacing and visual consistency.
- More robust input validation needed for event forms.


Group Members and Contributions:


1. Fatma Serra Koç
Designed and implemented the event creation and comment systems, including event cards and the main dashboard interface.
Developed the student vs. club manager role logic, integrated Firebase image uploads, and contributed to UI structure, FXML design, and debugging.


2. Eylül Naz Özcan
Managed UI layout and styling in FXML.
Writing reports and presentations
 

3. Utku Kabukçu
Developed backend Firebase structure and Firestore data model set up authentication and user session management contributed to UI structure, FXML design, and debugging fixed errors. 
Main contributor and creator of admin operations. Developed scene changing system and user model.

4. Hanne Koç
Forgot password system and UI design and styling
Writing reports and presentations 

5. Beyza Yılmaz
Managed UI layout and styling in FXML.
Writing reports and presentations


Software & Tools Used:

- Java JDK: 24.0.1  
- JavaFX SDK: 21.0.1  
- Firebase SDK (Firestore, Auth, Storage): 9.2.0  
- Maven: 4.0.0 (our pom.xml is compatible with maven 4.0.0)
- Scene Builder: Used for previewing scene
- IDE:  VSCode 1.102.1  
- Git & GitHub for version control  
- GitKraken for visual Git conflict management  
