package io.chrisdavenport.rank3

import org.specs2._

object CompileSpec extends mutable.Specification {

  val simple = {
    import cats._
    trait Foo[F[_]]{
      def foo: F[Int]
    }
    trait Bar[F[_]]{
      def bar: F[Int]
    }
    val transform = new ~~>[Tuple2R3[Functor, Foo, ?[_]], Bar] {
      def apply[F[_]](fh: Tuple2R3[Functor, Foo, F]): Bar[F] = new Bar[F]{
        def bar = fh._1.map(fh._2.foo)(_ + 1)
      }
    }
    val _ = transform
  }

  val compileCheckFunctionR3 = {
    trait Foo[F[_]]{
      def foo: F[Int]
    }
    trait Bar[F[_]]{
      def bar: F[Int]
    }
    val functionR3 = new ~~>[Foo, Bar]{
      def apply[F[_]](fh: Foo[F]): Bar[F] = new Bar[F]{
        def bar = fh.foo
      }
    }
    val foo = new Foo[Option]{
      def foo: Option[Int] = Some(1)
    }
    val bar = functionR3(foo)
    val _ = bar
  }

  val composition = {
    trait Foo[F[_]]{
      def foo: F[Int]
    }
    trait Bar[F[_]]{
      def bar: F[Int]
    }

    val functionR3 = new ~~>[Foo, Bar]{
      def apply[F[_]](fh: Foo[F]): Bar[F] = new Bar[F]{
        def bar = fh.foo
      }
    }

    trait Baz[F[_]]{
      def baz: F[(Int, Int)]
    }

    val foo = new Foo[Option]{
      def foo: Option[Int] = Some(1)
    }
    val bar = functionR3(foo)


    import cats._
    import cats.implicits._

    val transform = new ~~>[Tuple2R3[Monad, Tuple2R3[Foo, Bar, ?[_]], ?[_]], Baz]{
      def apply[F[_]](fh: Tuple2R3[Monad, Tuple2R3[Foo, Bar, ?[_]], F]): Baz[F] = new Baz[F] {
        def baz: F[(Int, Int)] = {
          implicit val M = fh._1
          val foo = fh._2._1
          val bar = fh._2._2
          for {
            foo <- foo.foo
            bar <- bar.bar
          } yield (foo, bar)
        }
      }
    }

    val test = Tuple2R3(Monad[Option], Tuple2R3(foo, bar))
    val _ = transform(test)
    
  }
  val function = {
        trait Foo[F[_]]{
      def foo: F[Int]
    }
    trait Bar[F[_]]{
      def bar: F[Int]
    }

    val functionR3 = new ~~>[Foo, Bar]{
      def apply[F[_]](fh: Foo[F]): Bar[F] = new Bar[F]{
        def bar = fh.foo
      }
    }

    trait Baz[F[_]]{
      def baz: F[(Int, Int)]
    }

    val foo = new Foo[Option]{
      def foo: Option[Int] = Some(1)
    }
    val bar = functionR3(foo)


    import cats._
    import cats.implicits._

    val function3 = new Function3R3[Monad, Foo, Bar, Baz]{
      def apply[F[_]](m: Monad[F], foo: Foo[F], bar: Bar[F]): Baz[F] = new Baz[F] {
        implicit val M = m
        def baz = for {
          foo <- foo.foo
          bar <- bar.bar
        } yield (foo, bar)
      }
    }
    val _ = function3(Monad[Option], foo, bar)
  }

}