# SkillSwap-Desktop-Java-Application
Peer-to-peer skill and knowledge exchange platform for students — Learn. Teach. Connect.
# SkillSwap

## Learn. Teach. Connect. 🎓

SkillSwap is a **Peer-to-Peer Community Skill & Knowledge Exchange Platform** designed for college students.

The platform connects students who want to **learn a skill** with peers who can **teach that skill**. It provides a simple way to create profiles, discover learning partners, schedule sessions, share resources, give ratings, and earn badges.

---

## 🚀 Problem Statement

Students often have valuable skills that they can teach to others, while other students are looking for exactly those skills to learn.

However, there is no simple campus-focused platform where students can easily:

- Find peers who can teach a specific skill
- Connect with suitable learning partners
- Schedule learning sessions
- Share notes and useful resources
- Give feedback after sessions
- Build recognition within the student community

**SkillSwap solves this problem by creating a simple peer-to-peer learning ecosystem.**

---

## 💡 Our Solution

SkillSwap creates a learning loop between students:

**Create Profile → Find a Peer → Request Session → Learn Together → Share Resources → Rate & Grow**

Students can list:

- Skills they can teach
- Skills they want to learn
- Their experience
- Their availability
- A short profile/bio

The application then helps them discover suitable learning partners.

---

## ✨ Key Features

### 👤 Student Profiles

Students can create profiles containing:

- Name
- Email
- Bio
- Experience
- Availability
- Skills they can teach
- Skills they want to learn

---

### 🔎 Peer Matchmaking

Students can search for peers based on the skills they want to learn.

For example:

> Priyanka wants to learn UI/UX.

If Rahul has UI/UX listed under **Skills I Can Teach**, SkillSwap can suggest Rahul as a learning partner.

---

### 📅 Session Planner

Students can:

- Send learning-session requests
- Select a skill
- Select date and time
- Accept requests
- Decline requests
- Track session status
- Mark completed sessions

Session states include:

**Pending → Accepted / Declined → Completed**

---

### 📝 Shared Notes & Resources

After or during a learning session, students can share:

- Notes
- Learning material
- Useful links
- Session-related resources

This helps students continue learning even after the session.

---

### ⭐ Peer Ratings

Students can rate their learning partners after completing a session.

The application supports:

- 1–5 star ratings
- Average rating calculation
- Rating count

This helps build a simple reputation system within the community.

---

### 🏆 Badges

Students can earn badges based on their participation and activity.

Available badges include:

- 🏆 Knowledge Champion
- 🎓 Skill Mentor
- 📚 Active Learner
- ⭐ Community Member

Badges encourage students to actively participate in teaching and learning.

---

## 🎨 UI/UX

SkillSwap was designed with a clean and colourful desktop interface.

The interface includes:

- Modern colour palette
- Rounded cards
- Improved buttons
- Better spacing and typography
- Profile cards
- Matchmaking cards
- Colour-coded session statuses
- Visual ratings
- Activity badges
- Consistent navigation

### Application Flow

**Welcome → Profiles → Dashboard → Matchmaking → Sessions → Notes → Ratings & Badges**

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| **Java 17** | Core programming language |
| **Java Swing** | Desktop graphical user interface |
| **Java OOP** | Application structure and data models |
| **Java Event Handling** | Button clicks and user interactions |
| **Java Object Serialization** | Local data persistence |
| **Local File Storage** | Stores application data |

---

## 🏗️ Architecture

The application follows a simple layered structure:

```text
                SkillSwap Application
                        │
                        ▼
                 ┌─────────────┐
                 │  UI Layer   │
                 │ Java Swing  │
                 └──────┬──────┘
                        │
                        ▼
                ┌──────────────┐
                │ Application  │
                │    Logic     │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │  Data Models │
                │ User         │
                │ Session      │
                │ Note         │
                │ AppData      │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │    Local     │
                │   Storage    │
                │ skillswap_   │
                │ data.dat     │
                └──────────────┘
