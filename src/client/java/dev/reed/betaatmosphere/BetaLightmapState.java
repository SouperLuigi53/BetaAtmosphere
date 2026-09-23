package dev.reed.betaatmosphere;

public interface BetaLightmapState {
    Parameters betaAtmosphere$getParameters();
    void betaAtmosphere$setParameters(Parameters parameters);

    /** Captured on the extraction thread, consumed on the render thread. Null means vanilla. */
    record Parameters(int skyDarkening, float ambient) {}
}
