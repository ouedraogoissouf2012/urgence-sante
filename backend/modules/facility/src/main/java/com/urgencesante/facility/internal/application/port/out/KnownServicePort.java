package com.urgencesante.facility.internal.application.port.out;

/** Port sortant : un code de service existe-t-il au catalogue ? */
public interface KnownServicePort {

    boolean isKnown(String serviceCode);
}
