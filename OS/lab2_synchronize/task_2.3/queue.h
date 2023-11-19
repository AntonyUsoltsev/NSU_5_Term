#ifndef __LAB2_SYNCHRONIZE__
#define __LAB2_SYNCHRONIZE__

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>

#define MAX_STRING_LENGTH 100
#define STORAGE_CAPACITY 15
#define THREAD_COUNT 3
#define ASC 0
#define DESC 1
#define EQ 2

typedef struct _Node {
    char value[MAX_STRING_LENGTH];
    struct _Node *next;
    pthread_mutex_t sync;
} Node;

typedef struct _Storage {
    Node *first;
} Storage;

typedef struct _ThreadData {
    Storage *storage;
    int *counter;
    // pthread_mutex_t *counter_mutex;
} ThreadData;


Storage* initialize_storage();

void add_node(Storage *storage, const char *value);

void fill_storage(Storage *storage);

void print_storage(Storage *storage);

//void swap_nodes(Node *prev1, Node *curr1, Node *prev2, Node *curr2);

void *ascending_thread(void *data);

void *descending_thread(void *data);

void *equal_length_thread(void *data);

void *count_monitor(void* arg);

//void *swap_thread(void *data);

#endif// __LAB2_SYNCHRONIZE__
