#ifndef __LAB2_SYNCHRONIZE__
#define __LAB2_SYNCHRONIZE__

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <errno.h>
#include <sys/types.h>
#include <unistd.h>
#include <pthread.h>
#include <assert.h>

typedef struct QueueNode {
	int val;
	struct QueueNode *next;
} qnode_t;

typedef struct Queue {
	qnode_t *first;
	qnode_t *last;

	pthread_t qmonitor_tid;

	int count;
	int max_count;

	// queue statistics
	long add_attempts;
	long get_attempts;
	long add_count;
	long get_count;
} queue_t;

queue_t* queue_init(int max_count);
void queue_destroy(queue_t *q);
int queue_add(queue_t *q, int val);
int queue_get(queue_t *q, int *val);
void queue_print_stats(queue_t *q);

#endif		// __LAB2_SYNCHRONIZE__
