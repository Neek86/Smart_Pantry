Smart Pantry 2026

MOBILE APP DEVELOPMENT 700  
Language: Java  
IDE: Android Studio  

App Description

Smart Pantry is an Android mobile application designed to help users track ingredients in their home pantry and suggest recipes based on available inventory. 
The application allows users to manage ingredient lists, create and browse recipes, match available items to dish requirements, and maintain an organized kitchen workflow.

Key Features

  Pantry Management	    : Add, edit, view, and delete pantry ingredients with quantities.
  Recipe Book			      : Store, update, and manage recipe lists and required ingredients.
  Smart Recipe Matcher  : Algorithmic matching engine that evaluates pantry items against saved recipes to suggest cooked options.
  Intuitive Navigation  : Hierarchical activity structure featuring Up button navigation across sub-activities.

Database Choice & Justification

  Database Option: SQLite (`DatabaseHelper`) For local data persistence, SQLite was selected as the embedded relational database management system.

Justification for SQLite:
  1. Zero Configuration & Embedded Native Support	: SQLite is built natively into the Android OS framework, eliminating the need for external server configurations or third-party cloud database dependencies.
  2. Structured Relational Storage				: The application relies on relational mappings (e.g., matching ingredients to specific recipes). SQLite's support for primary keys, foreign keys, and SQL query operations (`SELECT`, `JOIN`, `INSERT`, `UPDATE`, `DELETE`) makes it ideal for querying complex recipe-to-pantry relationships.
  3. Offline Reliability							: Since pantry inventory management is a local utility task, SQLite guarantees fast execution times, low overhead, and full offline accessibility without network latency or connectivity requirements.


Setup & Run Instructions

  Follow these steps to set up, build, and run the Smart Pantry application on a local machine or emulator.

Prerequisites
Android Studio (Electric Eel / Hedgehog / Ladybug or newer recommended)
JDK			: Java Development Kit 11 or 17
Android SDK	:API Level 24 (Android 7.0) or higher

