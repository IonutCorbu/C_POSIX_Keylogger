# Keylogger Implementation

## Overview

I implemented a basic solution for a keylogger in C using the POSIX API. The solution is meant to be tested on a Linux distribution. The keylogger runs in the background and captures keystrokes from a target system. 

---

## Future work

- To make the keylogger more difficult to detect, I will maintain communication with the server using a Command and Control (C2) communication paradigm. This will involve periodically sending heartbeat signals from infected systems to the server and receiving instructions, such as commands to transmit logged keystrokes.
- For server-side analysis of the received data, I will integrate a pattern-based search to identify potential credentials within the captured text. Additionally, a machine learning algorithm will be implemented to estimate the likelihood that specific character sequences are actual passwords. 

## Tasks to be Implemented

Here is a list of tasks for future development:

- [ ] **Include the possibility to do requests to the C2 server (backend);**
- [ ] **Use system to execute commands like `curl` for requests and responses with data to the server.**
- [ ] **Create a backend in a high-level language so it can be easier to work with files;**
- [ ] **Define actions for the keylogger that will be passed as responses for the requests initiated by the malware:**
    - [ ] Send keys logged for a victim;
    - [ ] Delete files on the victim computer so it does not seem suspicious.
    - [ ] Receive bulk data and save it in files for every victim;
- [ ] **Find a dataset to identify possible passwords in order to implement the ML-based algorithm;**
- [ ] **Find current implementations for algorithms dedicated for password recognition from a text;**
- [ ] **Evaluate the current keylogger level of exposure confronting with anti-virus solutions in Linux and further improve the solution to be harder to detect.**

---

## Requirements

- A Linux distribution (e.g., Ubuntu or CentOS)
- GCC compiler
- POSIX-compliant system

---

## How to Compile and Run

1. Clone the repository:

    ```bash
    git clone https://github.com/IonutCorbu/C_POSIX_Keylogger.git
    ```

2. Change to the project directory:

    ```bash
    cd C_POSIX_Keylogger
    ```

3. Compile the C code:

    ```bash
    gcc -o keylogger -lpthread keylogger.c
    ```

4. Run the keylogger:

    ```bash
    sudo ./keylogger
    ```

The keylogger will start capturing keystrokes and save them to the specified `output_file.txt`.

---

## Notes

- Ensure you have the necessary permissions to run the keylogger on the target machine.
- You can modify the code to suit specific requirements or perform further actions with the captured data.

---

## Future Directions

- Implement communication with a central server (C2) for real-time data transfer.
- Use machine learning algorithms to analyze the captured data for potential password identification.
