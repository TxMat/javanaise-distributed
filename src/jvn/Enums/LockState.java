package jvn.Enums;

// Lock states :
// No lock, Read, Write, Read cached, Write cached, Read + (Write cached)
public enum LockState {
    NL, R, W, RC, WC, RWC
}