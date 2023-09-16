#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>

// a)
// b) некоторые функции имеют cancellation point которые проверяют cancelation потока.
//    инкремент не имеет такой точки поэтому прерывания потока можно добится следующими действиями:
//    вызвать pthread_cancel()
//    задать pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS , NULL);
// c) pthread_cleanup_push(cleanup_function, str) устанавливает обработчки который выполнится при завершении потока

void cleanup_function(void *arg) {
    free((char *) arg);
    printf("memory cleanup\n");
}

void *mythread(void *arg) {


    printf("mythread tid = %ld\n", pthread_self());
   // pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    // pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, NULL);
    int i = 0;

    char *str = malloc(sizeof(char) * 12);
    if (str == NULL) {
        fprintf(stderr, "Bad malloc\n");
        return NULL;
    }
    strcpy(str, "hello world");
    pthread_cleanup_push(cleanup_function, str);

    while (1) {
        //i++;
        puts("Hello world!\n");
      //  puts(str);
       // pthread_testcancel();
    }

    pthread_cleanup_pop(1);
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
    pthread_join(tid, NULL);
    printf("Thread canceled.\n");
    return 0;
}


