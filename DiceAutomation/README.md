# Dice Automation

## Overview

Automates job search and Easy-Apply on Dice.com using Playwright in Java.

## Features

- Keyword-driven job search
- State-based location filtering
- Randomized human-like interactions
- Easy-Apply button automation
- Detailed logging of steps and failures

## Prerequisites

- Java 11 or newer
- Apache Maven
- Playwright Java (configured via Maven)
- Internet connection

## Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/benyamin-persia/DiceAuto.git
   ```
2. Navigate to the project directory:
   ```sh
   cd DiceAuto
   ```
3. (Optional) Configure credentials via environment variables:
   ```sh
   export DICE_EMAIL=your-email@example.com
   export DICE_PASSWORD=your-password
   ```
4. Install dependencies:
   ```sh
   mvn install
   ```

## Usage

Run the automation with:
```sh
mvn exec:java -Dexec.mainClass="dice.DiceAutomation.DiceLogin"
```
You will be prompted to enter job keywords; press Enter to use defaults.

## Configuration

- Default keywords and list of states are defined in `src/test/java/dice/DiceAutomation/DiceLogin.java`.
- Timeouts, selectors, and user agents can be adjusted in code.

## Logging

- Steps and actions are logged to `login_steps.log`.
- Failed job URLs are appended to `failed_jobs.csv`.

## Contributing

Pull requests and issues are welcome. Please ensure code follows project conventions and includes adequate logging and error handling.

## License

This project is distributed under the MIT License. 