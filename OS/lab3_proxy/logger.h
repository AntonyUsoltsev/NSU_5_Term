#ifndef LAB3_PROXY_LOGGER_H
#define LAB3_PROXY_LOGGER_H

#include <string.h>
#include <pthread.h>
#include <stdio.h>

#define LOG_BUFFER_SIZE 4096

#define RESET "\033[0m"
#define RED   "\033[1;31m"
#define GREEN "\033[1;32m"
#define YELLOW  "\033[1;33m"
#define BLUE  "\033[1;34m"
#define PURPLE  "\033[1;35m"
#define BACK_PURP "\033[1;45m"

void logg(char *msg, char *color) {
    pthread_t thread_id = pthread_self();
    if (strcmp(color, RED) == 0) {
        char buf[LOG_BUFFER_SIZE];
        sprintf(buf, "%s[Thread %ld] %s%s", color, thread_id, msg, RESET);
        perror(buf);
    } else {
        printf("%s[Thread %ld] %s%s\n", color, thread_id, msg, RESET);
    }
    fflush(stdout);
}

void logg_char(char *msg, char *info, char *color) {
    char buf[LOG_BUFFER_SIZE + 100];
    sprintf(buf, "%s %s", msg, info);
    logg(buf, color);
}

void logg_int(char *msg, long info, char *color) {
    char buf[LOG_BUFFER_SIZE + 100];
    sprintf(buf, "%s%ld", msg, info);
    logg(buf, color);
}

#endif //LAB3_PROXY_LOGGER_H
