#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>

void *mythread(void *arg) {
    printf("mythread tid = %ld\n", pthread_self());
    //pthread_detach(pthread_self());
    return NULL;
}

int main() {
    pthread_t tid;
    int err;
    pthread_attr_t attr;
    err = pthread_attr_init(&attr);

    if(err){
        printf("%s: attr init failed %s\n", __FUNCTION__ ,strerror(err));
        return -1;
    }

    err = pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

    if(err){
        printf("%s: attr init failed %s\n", __FUNCTION__ ,strerror(err));
        return -1;
    }

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;
    while (1) {
        err = pthread_create(&tid, NULL, mythread, NULL);
      //  printf("in main create new thread with tid %ld\n", tid);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }
    }
    return 0;
}

// main: pthread_create() failed: Resource temporarily unavailable
// Исчерпание ресурсов: Создание новых потоков в бесконечном цикле может потреблять большое количество системных
// ресурсов, таких как память и идентификаторы потоков (TID). Это может привести к исчерпанию ресурсов и в
// конечном итоге к сбою вашей программы или системы.


// Исчерпание ресурсов: Одной из наиболее распространенных причин является исчерпание системных ресурсов для создания
// новых потоков. Это может включать в себя ограничение на количество потоков, которое может быть создано в вашей
// системе, или ограничение на доступную память для новых потоков. Если система достигла своих лимитов по ресурсам,
// создание новых потоков временно становится невозможным.
//
//Лимиты на количество потоков: Некоторые операционные системы или конфигурации могут иметь ограничения на
// максимальное количество одновременно выполняющихся потоков в рамках одного процесса. Если этот лимит превышен,
// pthread_create() может вернуть ошибку.
//
//Лимиты ресурсов процесса: Ваш процесс также может иметь ограничения на использование ресурсов, такие как ограничения
// на максимальное количество дескрипторов файлов, которые могут быть открыты. Если эти лимиты достигнуты, создание
// новых потоков может быть временно невозможным.

// При добавлении pthread_detach() может создаться большее число потоков