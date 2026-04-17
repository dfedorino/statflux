package com.rmrf.statflux.util.result;

public sealed interface Result<T> permits Failure, Success {

}
