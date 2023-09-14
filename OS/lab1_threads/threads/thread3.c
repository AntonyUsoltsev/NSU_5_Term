#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>

typedef struct {
    int param1;
    char *param2;
} thread_args;

void *mythread(void *arg) {
    sleep(10);
    printf("mythread tid = %ld\n", pthread_self());
    thread_args *input_args = (thread_args *) arg;
    printf("Struct params: %d, %c\n", input_args->param1, *input_args->param2);

    return NULL;
}


// В случае detached потока струкутуру надо располагать в общей памяти (куче)

int main() {
    pthread_t tid;
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;
     thread_args *args = malloc(sizeof(thread_args));
    //thread_args args;
    args->param1 = 1;
    args->param2 = "a";
    err = pthread_create(&tid, NULL, mythread, &args);
    pthread_detach(tid);
    //  printf("in main create new thread with tid %ld\n", tid);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }
    // pthread_join(tid, NULL);
    // sleep(10);
    pthread_exit(0);
    return 0;
}
