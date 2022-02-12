package de.proto4j.stream;//@date 12.02.2022

import java.lang.annotation.Annotation;

public interface AnnotationStream<A extends Annotation> extends SequenceStream<A> {

    Class<A> annotationType();

}
