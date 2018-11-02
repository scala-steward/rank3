package io.chrisdavenport.rank3

import cats.implicits._
import typeclasses.FunctorR3

final case class EitherR3[F[_[_]], G[_[_]], H[_]](run: Either[F[H], G[H]]){
  def left: Option[F[H]] = run.fold(_.some, _ => None)
  def right: Option[G[H]] = run.fold(_ => None, _.some)

  def mapR3[N[_[_]]](f: G ~~> N): EitherR3[F, N, H] = 
    semiFold(EitherR3.leftc(_), gh => EitherR3.rightc(f(gh)))
  def leftMapR3[N[_[_]]](f: F ~~> N): EitherR3[N, G, H] =
    semiFold(fh => EitherR3.leftc(f(fh)), EitherR3.rightc(_))

  def semiFold[C](left: F[H] => C, right: G[H] => C): C = run.fold(left(_), right(_))
  def fold[M[_[_]]](left: F ~~> M, right: G ~~> M): M[H] = run.fold(left(_), right(_))
}
object EitherR3 {
  def leftc[F[_[_]], G[_[_]], H[_]](left: F[H]): EitherR3[F, G, H] = EitherR3(Left(left))
  def rightc[F[_[_]], G[_[_]], H[_]](right: G[H]): EitherR3[F, G, H] = EitherR3(Right(right))

  // Could just a easily be a Bifunctor
  implicit def functorEitherR3[F[_[_]], H[_]] = {
    type PartiallyApliedEitherR3[G[_[_]]] = EitherR3[F, G, H]
    new FunctorR3[PartiallyApliedEitherR3]{
      def mapR3[M[_[_]], N[_[_]]](fa: EitherR3[F, M, H])(f: M ~~> N): EitherR3[F, N, H] = 
        fa.mapR3(f)
    }
  }
}