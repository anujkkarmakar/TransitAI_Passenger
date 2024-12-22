# Contributing to TransitAI Passenger 🚌

First off, thanks for taking the time to contribute! 🎉

## Table of Contents
- [Getting Started](#getting-started)
- [Setting Up Development Environment](#setting-up-development-environment)
- [Pull Request Process](#pull-request-process)
- [Coding Guidelines](#coding-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Bug Reports](#bug-reports)
- [Feature Requests](#feature-requests)

## Getting Started
1. Fork the repository
2. Clone your fork:
```bash
git clone https://github.com/your-username/TransitAI_Passenger.git
```
3. Add upstream remote:
```bash
git remote add upstream https://github.com/anujkkarmakar/TransitAI_Passenger.git
```

## Setting Up Development Environment
1. Required Tools:
   - Android Studio Arctic Fox or later
   - JDK 15 or higher
   - Android SDK API 32 or higher
   - Firebase account

2. Setup Steps:
   - Open project in Android Studio
   - Add your `google-services.json` file
   - Sync project with Gradle files
   - Build and run

## Pull Request Process
1. Create a new branch:
```bash
git checkout -b feature/your-feature-name
```

2. Make your changes:
   - Write clean, readable code
   - Follow coding guidelines
   - Add comments where necessary
   - Update documentation if needed

3. Commit your changes:
```bash
git add .
git commit -m "Description of changes"
git push origin feature/your-feature-name
```

4. Create Pull Request:
   - Go to GitHub
   - Create PR from your branch to main
   - Fill in PR template
   - Wait for review

## Coding Guidelines

### Java Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Keep methods short and focused
- Add appropriate comments
- Follow MVVM architecture pattern

### XML Style
- Use descriptive IDs
- Keep layouts clean and efficient
- Follow material design guidelines
- Use styles and themes appropriately

### Firebase Guidelines
- Keep data structure flat
- Use appropriate security rules
- Follow Firebase best practices
- Optimize queries

## Commit Message Guidelines
Format:
```
type(scope): subject

[optional body]

[optional footer]
```

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- style: Formatting
- refactor: Code restructuring
- test: Adding tests
- chore: Maintenance

Example:
```
feat(auth): add Google Sign-In functionality

Implement Google Sign-In using Firebase Authentication
Add necessary UI elements and handlers
Update documentation
```

## Bug Reports
When filing an issue, please include:
- Description of the issue
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots (if applicable)
- Device information
- Android version
- App version

## Feature Requests
When suggesting a feature:
- Clearly describe the feature
- Explain the use case
- Provide examples if possible
- Consider implementation challenges

## Questions or Need Help?
Feel free to:
- Open an issue
- Email at anujkarmakar999@gmail.com
- Check existing documentation

## License
By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to TransitAI Passenger! 🎉
