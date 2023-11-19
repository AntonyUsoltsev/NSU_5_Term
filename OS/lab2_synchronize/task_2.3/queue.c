#include <unistd.h>
#include "queue.h"

int main() {
    Storage *storage = initialize_storage();

    fill_storage(storage);

    print_storage(storage);

    pthread_t ascending_tid, descending_tid, equal_length_tid, swap_tid, monitor;

    int *counters = calloc(THREAD_COUNT, sizeof(int));

    ThreadData ascending_data = {storage, &counters[ASC]};
    ThreadData descending_data = {storage, &counters[DESC]};
    ThreadData equal_length_data = {storage, &counters[EQ]};

    pthread_create(&ascending_tid, NULL, ascending_thread, &ascending_data);
    pthread_create(&descending_tid, NULL, descending_thread, &descending_data);
    pthread_create(&equal_length_tid, NULL, equal_length_thread, &equal_length_data);
    pthread_create(&monitor, NULL, count_monitor, counters);
    // pthread_create(&swap_tid, NULL, swap_thread, storage);

    pthread_join(ascending_tid, NULL);
    pthread_join(descending_tid, NULL);
    pthread_join(equal_length_tid, NULL);
    pthread_join(monitor, NULL);
    // pthread_join(swap_tid, NULL);

    return 0;
}

Storage *initialize_storage() {
    Storage *storage = malloc(sizeof(Storage));
    if (!storage) {
        printf("Failed to allocate memory for a queue\n");
        abort();
    }
    storage->first = NULL;
    return storage;
}

void add_node(Storage *storage, const char *value) {
    Node *new_node = (Node *) malloc(sizeof(Node));

    if (!new_node) {
        perror("Failed to allocate memory for a new node");
        exit(EXIT_FAILURE);
    }
    if (storage->first != NULL) {
        Node *node = storage->first;
        while (node->next != NULL) {
            node = node->next;
        }
        node->next = new_node;
    } else {
        storage->first = new_node;
    }
    strcpy(new_node->value, value);
    new_node->next = NULL;
    pthread_mutex_init(&(new_node->sync), NULL);
}

void fill_storage(Storage *storage) {
    for (int i = 0; i < STORAGE_CAPACITY; ++i) {
        char buff[10];
        sprintf(buff, "%d", (i + 5) % STORAGE_CAPACITY);
        add_node(storage, buff);
    }
}

void print_storage(Storage *storage) {
    Node *current = storage->first;
    while (current != NULL) {
        printf("%s ", current->value);
        current = current->next;
    }
    printf("\n");
}


void *ascending_thread(void *data) {
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
            pthread_mutex_lock(&(curr->sync));

            volatile int pair_count = 0;
            if (strlen(prev->value) < strlen(curr->value)) {
                pair_count++;
            }

            pthread_mutex_unlock(&(prev->sync));
            pthread_mutex_unlock(&(curr->sync));

            prev = curr;
            curr = curr->next;
        }
    }

    return NULL;
}

void *descending_thread(void *data) {
    ThreadData *thread_data = (ThreadData *) data;
    Storage *storage = thread_data->storage;
    int *counter = thread_data->counter;
    //  pthread_mutex_t *counter_mutex = thread_data->counter_mutex;

    while (1) {
        //    pthread_mutex_lock(counter_mutex);
        (*counter)++;
        //   pthread_mutex_unlock(counter_mutex);

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
            pthread_mutex_lock(&(curr->sync));

            volatile int pair_count = 0;
            if (strlen(prev->value) > strlen(curr->value)) {
                pair_count++;

            }

            pthread_mutex_unlock(&(prev->sync));
            pthread_mutex_unlock(&(curr->sync));
            prev = curr;
            curr = curr->next;
        }
        //   printf("Desc count: %d\n", *counter);
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
            pthread_mutex_lock(&(curr->sync));

            volatile int pair_count = 0;
            if (strlen(prev->value) == strlen(curr->value)) {
                pair_count++;
            }

            pthread_mutex_unlock(&(prev->sync));
            pthread_mutex_unlock(&(curr->sync));

            prev = curr;
            curr = curr->next;
        }
        //  printf("Eq count: %d\n", *counter);
    }

    return NULL;
}

void swap_nodes(Node *prev1, Node *curr1, Node *prev2, Node *curr2) {
    if (prev1 != NULL) {
        prev1->next = curr2;
    } else {
        // If prev1 is NULL, curr1 is the head of the list
        // Update the head of the list
        prev1 = curr2;
    }

    if (prev2 != NULL) {
        prev2->next = curr1;
    } else {
        // If prev2 is NULL, curr2 is the head of the list
        // Update the head of the list
        prev2 = curr1;
    }

    // Swap next pointers
    Node *temp = curr1->next;
    curr1->next = curr2->next;
    curr2->next = temp;
}

//
//void *swap_thread(void *data) {
//    Storage *storage = (Storage *) data;
//
//    while (1) {
//        Node *prev1 = NULL;
//        Node *curr1 = storage->first;
//
//        Node *prev2 = NULL;
//        Node *curr2 = storage->first->next;
//
//        while (curr2 != NULL) {
//            pthread_mutex_lock(&(curr1->sync));
//            pthread_mutex_lock(&(curr2->sync));
//
//            // Critical section
//            // Simulate random condition for swapping nodes
//            if (rand() % 2 == 0) {
//                // Swap nodes
//                swap_nodes(prev1, curr1, prev2, curr2);
//
//                // Print after successful swap
//                printf("Swapped: ");
//                print_storage(storage);
//            }
//
//            pthread_mutex_unlock(&(curr1->sync));
//            pthread_mutex_unlock(&(curr2->sync));
//
//            prev1 = curr1;
//            curr1 = curr1->next;
//
//            prev2 = curr2;
//            curr2 = curr2->next;
//        }
//    }
//
//    return NULL;
//}

void *count_monitor(void *arg) {
    while (1) {
        int *counters = (int *) arg;
        printf("ASC: %d, DESC: %d, EQ: %d\n", counters[ASC], counters[DESC], counters[EQ]);
        sleep(1);
    }
    return NULL;
}