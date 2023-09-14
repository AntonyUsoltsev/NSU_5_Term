#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>

void cleanup_function(void *arg) {
    free((char *) arg);
}

void *mythread(void *arg) {


    printf("mythread tid = %ld\n", pthread_self());
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, NULL);
    int i = 0;

    char *str = malloc(sizeof(char) * 12);
    if (str == NULL) {
        fprintf(stderr, "Bad malloc\n");
        return NULL;
    }
    strcpy(str, "hello world");
    pthread_cleanup_push(cleanup_function, str) ;
    pthread_cleanup_pop(1);
    while (1) {
        //i++;
        //puts("Hello world!\n");
        puts(str);
        pthread_testcancel();
    }

    return NULL;
}

int main() {
    pthread_t tid;
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;

    err = pthread_create(&tid, NULL, mythread, NULL);
    //pthread_detach(tid);
    //  printf("in main create new thread with tid %ld\n", tid);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }
    sleep(2);
    pthread_cancel(tid);
    printf("Thread canceled.\n");
    pthread_join(tid, NULL);
    return 0;
}


