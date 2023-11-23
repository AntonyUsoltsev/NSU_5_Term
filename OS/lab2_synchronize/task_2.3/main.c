#include <unistd.h>
#include <pthread.h>
#include <stdbool.h>
#include "queue.h"

#define STORAGE_CAPACITY 8
#define THREAD_COUNT 4
#define ASC 0
#define DESC 1
#define EQ 2
#define SWAP 3

void lock(Node *elem, int line, const int *counter, char *type) {
    printf("%d: %s try lock in %d, on value %s \n", *counter, type, line + 1, elem->value);
    fflush(stdout);
    pthread_mutex_lock(&elem->sync);
    printf("%d: %s mutex lock in line %d, on value %s \n", *counter, type, line - 1, elem->value);
    fflush(stdout);
}

void unlock(Node *elem, int line, const int *counter, char *type) {
//    printf("%d: %s try unlock in %d, on value %s \n", *counter, type, line + 1, elem->value);
//    fflush(stdout);
    pthread_mutex_unlock(&elem->sync);
//    printf("%d: %s mutex unlock in line %d, on value %s \n", *counter, type, line - 1, elem->value);
//    fflush(stdout);
}

void *ascending_thread(void *data) {
    ThreadData *thread_data = (ThreadData *) data;
    Storage *storage = thread_data->storage;
    int *counter = thread_data->counter;
    while (1) {
        Node *prev = storage->first;
        Node *curr = NULL;
        if (prev != NULL) {
            curr = prev->next;
        } else {
            printf("Too few elements in queue");
            break;
        }

        while (curr != NULL && prev !=NULL) {
            if (pthread_mutex_trylock(&prev->sync) == 0) {
                if (pthread_mutex_trylock(&curr->sync) == 0) {
                    volatile int pair_count = 0;
                    if (strlen(prev->value) < strlen(curr->value)) {
                        pair_count++;
                    }
                    unlock(curr, __LINE__, counter, "asc");
                }
                unlock(prev, __LINE__, counter, "asc");
            }
            prev = prev->next;
            curr = curr->next;
            (*counter)++;

        }
    }

    return NULL;
}

void *descending_thread(void *data) {
    ThreadData *thread_data = (ThreadData *) data;
    Storage *storage = thread_data->storage;
    int *counter = thread_data->counter;

    while (1) {
        (*counter)++;
        Node *prev = storage->first;
        Node *curr = NULL;
        if (prev != NULL) {
            curr = storage->first->next;
        } else {
            printf("Too few elements in queue");
            break;
        }

        while (curr != NULL) {
            pthread_mutex_lock(&(prev->sync));
            printf("desc mutex lock in line %d, on value %s \n", __LINE__ - 1, prev->value);
            fflush(stdout);
            pthread_mutex_lock(&(curr->sync));
            printf("desc mutex lock in line %d, on value %s \n", __LINE__ - 1, curr->value);
            fflush(stdout);

            volatile int pair_count = 0;
            if (strlen(prev->value) > strlen(curr->value)) {
                pair_count++;
            }

            Node *tmp = prev;
            prev = curr;
            curr = curr->next;
            pthread_mutex_unlock(&(prev->sync));
            printf("desc mutex unlock in line %d, on value %s \n", __LINE__ - 1, prev->value);
            fflush(stdout);
            pthread_mutex_unlock(&(tmp->sync));
            printf("desc mutex unlock in line %d, on value %s \n", __LINE__ - 1, tmp->value);
            fflush(stdout);

        }
        //  printf("Desc count: %d\n", *counter);
    }

    return NULL;
}

void *equal_length_thread(void *data) {
    ThreadData *thread_data = (ThreadData *) data;
    Storage *storage = thread_data->storage;
    int *counter = thread_data->counter;
    //pthread_mutex_t *counter_mutex = thread_data->counter_mutex;

    while (1) {
        //  pthread_mutex_lock(counter_mutex);
        (*counter)++;
        // pthread_mutex_unlock(counter_mutex);

        Node *prev = storage->first;
        Node *curr = NULL;
        if (prev != NULL) {
            curr = storage->first->next;
        } else {
            printf("Too few elements in queue");
            break;
        }

        while (curr != NULL) {
            pthread_mutex_lock(&(prev->sync));
            printf("eq mutex lock in line %d, on value %s \n", __LINE__ - 1, prev->value);
            fflush(stdout);
            pthread_mutex_lock(&(curr->sync));
            printf("eq mutex lock in line %d, on value %s \n", __LINE__ - 1, curr->value);
            fflush(stdout);

            volatile int pair_count = 0;
            if (strlen(prev->value) == strlen(curr->value)) {
                pair_count++;
            }

            Node *tmp = prev;
            prev = curr;
            curr = curr->next;

            pthread_mutex_unlock(&(prev->sync));
            printf("eq mutex unlock in line %d, on value %s \n", __LINE__ - 1, prev->value);
            fflush(stdout);
            pthread_mutex_unlock(&(tmp->sync));
            printf("eq mutex unlock in line %d, on value %s \n", __LINE__ - 1, tmp->value);
            fflush(stdout);

        }
    }

    return NULL;
}

void swap_nodes(Node *node1, Node *node2, Node *node3) { //swaps node2 and node3
    if (node1 != NULL) {
        node2->next = node3->next;
        node1->next = node3;
        node3->next = node2;
    } else {
        node2->next = node3->next;
        node3->next = node2;
    }
}


void *swap_thread(void *data) {
    ThreadData *thread_data = (ThreadData *) data;
    Storage *storage = thread_data->storage;
    int *counter = thread_data->counter;
    int a = 0;
    while (1) {
        Node *curr1 = storage->first;
        Node *curr2 = curr1->next;
        Node *curr3 = curr2->next;
        while (curr3 != NULL) {
            if (pthread_mutex_trylock(&curr1->sync) == 0) {
                if (pthread_mutex_trylock(&curr2->sync) == 0) {
                    if (pthread_mutex_trylock(&curr3->sync) == 0) {
                        if (rand() % 2 == 0) {
                            curr2->next = curr3->next;
                            curr3->next = curr2;
                            curr1->next = curr3;
                            (*counter)++;
                        }
                        unlock(curr3, __LINE__, counter, "swap");
                    }
                    unlock(curr2, __LINE__, counter, "swap");
                }
                unlock(curr1, __LINE__, counter, "swap");
            }
            curr1 = curr1->next;
            curr2 = curr1->next;
            curr3 = curr2->next;


        }

    }
}


void *count_monitor(void *arg) {
    int *counters = (int *) arg;
    while (1) {
        printf("ASC: %d, DESC: %d, EQ: %d, SWAP: %d\n", counters[ASC], counters[DESC], counters[EQ], counters[SWAP]);
        sleep(1);
    }
    return NULL;
}

int main() {
    Storage *storage = initialize_storage(STORAGE_CAPACITY);
    fill_storage(storage);
    print_storage(storage);

    pthread_t ascending_tid, descending_tid, equal_length_tid, swap_tid, monitor;

    int *counters = calloc(THREAD_COUNT, sizeof(int));

    ThreadData ascending_data = {storage, &counters[ASC]};
    ThreadData descending_data = {storage, &counters[DESC]};
    ThreadData equal_data = {storage, &counters[EQ]};
    ThreadData swap_data = {storage, &counters[SWAP]};

    pthread_create(&ascending_tid, NULL, ascending_thread, &ascending_data);
    //   pthread_create(&descending_tid, NULL, descending_thread, &descending_data);
    //  pthread_create(&equal_length_tid, NULL, equal_length_thread, &equal_data);
    pthread_create(&swap_tid, NULL, swap_thread, &swap_data);
    pthread_create(&monitor, NULL, count_monitor, counters);


    pthread_join(ascending_tid, NULL);
//    pthread_join(descending_tid, NULL);
//    pthread_join(equal_length_tid, NULL);
    pthread_join(monitor, NULL);
    pthread_join(swap_tid, NULL);

    return 0;
}