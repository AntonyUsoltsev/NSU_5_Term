#define _GNU_SOURCE
#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>


void *mythread(void *arg) {
    printf("mythread [%d %d %d]: Hello from mythread!\n", getpid(), getppid(), gettid());
    //// int result = 42
    ////  return (void *) result; так не будет работать, потому что по завершении потока его стек разрушится
    int *ret_val = malloc(sizeof(int));
    if (ret_val == NULL) {
        fprintf(stderr, "Bad malloc\n");
        return NULL;
    }
    *ret_val = 42;
//    char* ret_str = malloc(sizeof (char) * 12);
//    if (ret_str == NULL) {
//        fprintf(stderr, "Bad malloc\n");
//        return NULL;
//    }
//    strcpy(ret_str, "hello world");
    return (void *) ret_val;
}

//a: pthread_join(tid, &thread_result);

/////b: необходимо выделить память на куче для return value ????


int main() {
    pthread_t tid;
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;

    err = pthread_create(&tid, NULL, mythread, NULL);
    printf("in main create new thread with tid %ld\n", tid);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }
//  void *thread_result = malloc(sizeof(int));
    void *thread_result = malloc(sizeof(char) * 12);

    pthread_join(tid, &thread_result);

//  printf("Thread returned: %d\n", *(int *) thread_result);
    printf("Thread returned: %d\n", *(int*) thread_result);

    free(thread_result);
    return 0;
}

