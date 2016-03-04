package sample;

import static akka.pattern.Patterns.ask;

import akka.dispatch.Futures;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sample.CountingActor.Count;
import sample.CountingActor.Get;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * A main class to start up the application.
 */
public class Main {
  public static void main(String[] args) throws Exception {
    // create a spring context and scan the classes
    AnnotationConfigApplicationContext ctx =
      new AnnotationConfigApplicationContext();
    ctx.scan("sample");
    ctx.refresh();

    // get hold of the actor system
    ActorSystem system = ctx.getBean(ActorSystem.class);

    // use the Spring Extension to create props for a named actor bean
    ActorRef counter = system.actorOf(
      SpringExtension.factory.get(system).props("CountingActor"), "counter");

    // now do the same thing using the new Java8 lambda syntax
    ActorRef counter2 = system.actorOf(
      SpringExtension.factory.get(system).props("CountingLambdaActor"), "counter2");

    counter.tell(new Count(), null);
    counter2.tell(new Count(), null);
    counter.tell(new Count(), null);
    counter2.tell(new Count(), null);
    counter.tell(new Count(), null);
    counter2.tell(new Count(), null);

    final ExecutionContext ec = system.dispatcher();
    FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
    ArrayList<Future<Object>> futures = new ArrayList<>();
    futures.add(ask(counter, new Get(), Timeout.durationToTimeout(duration)));
    futures.add(ask(counter2, new Get(), Timeout.durationToTimeout(duration)));
    try {
      Iterable<Object> results = Await.result(Futures.sequence(futures, ec), duration);
      for (Object i : results) {
        // print the result
        System.out.println("Got back " + i);
      }
    } catch (Exception e) {
      System.err.println("Failed getting result: " + e.getMessage());
      throw e;
    } finally {
      system.shutdown();
      system.awaitTermination();
    }
  }
}
