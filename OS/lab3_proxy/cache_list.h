#ifndef LAB3_PROXY_CACHE_LIST_H
#define LAB3_PROXY_CACHE_LIST_H

#include <pthread.h>
#include <stdlib.h>
#include "logger.h"

#define CACHE_BUFFER_SIZE (1024 * 1024 * 64) // 64 MB

typedef struct cache {
    unsigned long request;
    char *response;
    ssize_t response_len;
    struct cache *next;
} Cache;

pthread_mutex_t cache_mutex = PTHREAD_MUTEX_INITIALIZER;

unsigned long hash(const char *str) {
    unsigned long hash = 0;
    while (*str != '\0') {
        // hash * 33 + cur symbol
        hash = ((hash << 5) + hash) + (unsigned long) (*str);
        str++;
    }
    return hash;
}

int init_cache_record(Cache *record) {
    record->response = (char *) calloc(CACHE_BUFFER_SIZE, sizeof(char));
    if (record->response == NULL) {
        logg("Failed to allocate memory to new response array", RED);
        return EXIT_FAILURE;
    }
    record->next = NULL;
    return EXIT_SUCCESS;
}

ssize_t find_in_cache(Cache *start, char *req, char *copy) {
    Cache *cur = start;
    pthread_mutex_lock(&cache_mutex);
    unsigned long req_hash = hash(req);
    while (cur != NULL) {
        if (cur->request == req_hash) {
            strncpy(copy, cur->response, cur->response_len);
            pthread_mutex_unlock(&cache_mutex);
            return cur->response_len;
        }
        cur = cur->next;
    }
    pthread_mutex_unlock(&cache_mutex);
    return -1;
}

void add_request(Cache *record, char *req) {
    unsigned long _hash = hash(req);
    logg_int("Current hash = ", _hash, YELLOW);
    record->request = _hash;

}

void add_response(Cache *record, char *resp, unsigned long cur_position, unsigned long resp_size) {
    memcpy(record->response + cur_position, resp, resp_size);
}

void add_size(Cache *record, ssize_t size) {
    record->response_len = size;
}

// Предупреждение: вставка записи в кэш происходит после того как клиенту был передан весь ответ,
// поэтому если после получения результата первого запроса сразу сделать такой же запрос еще раз, то данные первого
// запроса могут еще не быть в кэше
void push_record(Cache *start, Cache *record) {
    Cache *cur = start;
    pthread_mutex_lock(&cache_mutex);
    logg("Starting caching", YELLOW);
    while (cur->next != NULL) {
        cur = cur->next;
    }
    cur->next = record;
    pthread_mutex_unlock(&cache_mutex);
}

void delete_cache_record(Cache *record) {
    free(record->response);

}

void print_cache(Cache *start) {
    pthread_mutex_lock(&cache_mutex);
    int i = 0;
    Cache *cur = start;
    printf("Print cache:\n");
    while (cur != NULL) {
        printf("%d: hash = %ld\n", i, cur->request);
        cur = cur->next;
        i++;
    }
    pthread_mutex_unlock(&cache_mutex);
}

#endif //LAB3_PROXY_CACHE_LIST_H
