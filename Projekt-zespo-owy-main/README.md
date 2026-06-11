# QuizLoop

QuizLoop is a small Kotlin + Jetpack Compose Android app for a university project. It keeps the initial scope intentionally compact while leaving space to grow into a Firebase-backed quiz platform later.

## What it demonstrates

- A modern single-activity Compose UI
- Two core flows: creating quizzes and joining/playing a live quiz room
- A repository-first structure that can be swapped to Firebase later
- Small, readable feature packages that are easy to expand

## Project structure

- `core/model` for quiz and session data
- `core/data` for the repository abstraction and in-memory demo implementation
- `feature/home` for the landing screen
- `feature/create` for quiz draft creation
- `feature/join` for entering a room code and joining a live room
- `feature/play` for the interactive quiz round
- `ui/theme` for app colors and Material 3 styling

## Next expansion ideas

- Replace the in-memory repository with Firestore or Realtime Database
- Add Firebase Authentication for host and player identity
- Persist quiz drafts and session results in a cloud backend
- Add real-time multiplayer synchronization and scoreboard updates
