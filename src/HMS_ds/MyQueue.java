package HMS_ds;



public class MyQueue<T> {
    class Node {
        T data;
        Node next;
        Node(T data) {
            this.data = data;
        }
    }

    Node front, rear;
    int size = 0;

    public void enqueue(T data) {
        Node newNode = new Node(data);
        if (rear != null) {
            rear.next = newNode;
        }
        rear = newNode;
        if (front == null) {
            front = rear;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) return null;
        T data = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return data;
    }

    public T peek() {
        return isEmpty() ? null : front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public void display() {
        Node current = front;
        System.out.print("Waiting List: ");
        while (current != null) {
            System.out.print(current.data + " → ");
            current = current.next;
        }
        System.out.println("null");
     }
}