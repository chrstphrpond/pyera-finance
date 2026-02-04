# Contributing to Pyera Finance

Thank you for your interest in contributing to Pyera Finance! This document provides guidelines and steps for contributing.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

### Reporting Bugs

1. **Check existing issues** - Search the [issue tracker](https://github.com/chrstphrpond/pyera-finance/issues) to avoid duplicates
2. **Create a detailed report** with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Device info (Android version, device model)
   - Screenshots or logs if applicable

### Suggesting Features

1. Open an issue with the `enhancement` label
2. Describe the feature and its use case
3. Explain why it would benefit users

### Pull Requests

1. **Fork the repository**

   ```bash
   git clone https://github.com/YOUR_USERNAME/pyera-finance.git
   ```

2. **Create a feature branch**

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow the existing code style
   - Write meaningful commit messages
   - Add tests for new functionality

4. **Test your changes**

   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   Then open a Pull Request on GitHub.

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 34

### Getting Started

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Copy `local.properties.example` to `local.properties` and add required keys
5. Run on an emulator or device

## Code Style

### Kotlin

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Document public APIs with KDoc

### Compose

- Use `@Preview` for all composable functions
- Extract reusable components to `ui/components/`
- Follow Material 3 design guidelines
- Use theme colors and typography from `ui/theme/`

### Architecture

We follow **Clean Architecture** with **MVVM**:

```
data/          → Repositories, data sources, models
domain/        → Use cases, business logic
ui/            → ViewModels, Composables, UI state
di/            → Dependency injection modules
```

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add budget category picker
fix: resolve crash on transaction delete
docs: update README installation steps
refactor: extract common button component
test: add unit tests for BudgetViewModel
```

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Linting

```bash
./gradlew lint
```

## Review Process

1. All PRs require at least one review
2. CI checks must pass
3. No merge conflicts
4. Code coverage should not decrease

## Questions?

Feel free to open an issue or reach out to the maintainers.

---

Thank you for contributing! 🎉
