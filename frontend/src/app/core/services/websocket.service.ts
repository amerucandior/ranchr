import { Injectable, OnDestroy, signal } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private client: Client | null = null;
  readonly isConnected = signal<boolean>(false);

  private readonly topicSubjects = new Map<string, Subject<unknown>>();
  private readonly activeSubscriptions = new Map<string, StompSubscription>();

  constructor() {
    this.initClient();
  }

  private initClient(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.isConnected.set(true);
        this.resubscribeTopics();
      },
      onDisconnect: () => {
        this.isConnected.set(false);
      },
      onStompError: (frame) => {
        console.error('STOMP Broker error:', frame.headers['message'], frame.body);
      },
    });

    this.client.activate();
  }

  subscribe<T>(topic: string): Observable<T> {
    if (!this.topicSubjects.has(topic)) {
      const subject = new Subject<unknown>();
      this.topicSubjects.set(topic, subject);

      if (this.isConnected() && this.client) {
        this.bindSubscription(topic, subject);
      }
    }

    return this.topicSubjects.get(topic)!.asObservable() as Observable<T>;
  }

  send(destination: string, body: unknown): void {
    if (this.client && this.isConnected()) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.warn('Cannot send message, WebSocket not connected:', destination);
    }
  }

  private resubscribeTopics(): void {
    this.activeSubscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch {
        // ignore
      }
    });
    this.activeSubscriptions.clear();

    this.topicSubjects.forEach((subject, topic) => {
      this.bindSubscription(topic, subject);
    });
  }

  private bindSubscription(topic: string, subject: Subject<unknown>): void {
    if (!this.client || !this.isConnected()) {
      return;
    }
    const sub = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body);
        subject.next(payload);
      } catch {
        subject.next(message.body);
      }
    });
    this.activeSubscriptions.set(topic, sub);
  }

  ngOnDestroy(): void {
    this.activeSubscriptions.forEach((sub) => sub.unsubscribe());
    this.activeSubscriptions.clear();
    this.topicSubjects.forEach((sub) => sub.complete());
    this.topicSubjects.clear();
    if (this.client) {
      this.client.deactivate();
    }
  }
}
