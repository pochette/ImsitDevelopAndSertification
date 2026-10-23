package ru.burdak.chatbot.service;

import org.junit.jupiter.api.RepeatedTest;
import ru.burdak.chatbot.repository.InMemorySessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Одновременные закрытие сеанса и запись сообщений не восстанавливают удалённый сеанс.
 */
class ChatServiceConcurrencyTest {

  @RepeatedTest(20)
  void concurrentLogoutAndWritesDoNotRestoreSession() throws Exception {
    var service = new ChatService(new InMemorySessionRepository(), Clock.systemUTC(),
                                  Duration.ofMinutes(15), 10_000, 4096);
    UUID id = service.login().sessionId();

    ExecutorService pool = Executors.newFixedThreadPool(8);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> tasks = new ArrayList<>();
    try {
      for (int i = 0; i < 200; i++) {
        tasks.add(pool.submit(() -> {
          start.await();
          try {
            service.sendMessage(id, "Сообщение");
          } catch (NoSuchElementException expectedAfterLogout) {
            // запись после закрытия сеанса отклоняется
          }
          return null;
        }));
      }
      tasks.add(pool.submit(() -> {
        start.await();
        service.logout(id);
        return null;
      }));

      start.countDown();
      for (Future<?> task : tasks) {
        task.get(10, TimeUnit.SECONDS);
      }
    } finally {
      pool.shutdownNow();
    }

    assertThrows(NoSuchElementException.class, () -> service.getSession(id));
    assertThrows(NoSuchElementException.class, () -> service.sendMessage(id, "После выхода"));
  }
}