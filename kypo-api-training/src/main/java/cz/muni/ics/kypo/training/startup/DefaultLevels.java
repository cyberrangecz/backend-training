package cz.muni.ics.kypo.training.startup;

import javax.validation.Valid;

public class DefaultLevels {
    @Valid
    private IntroInfoLevel introInfoLevel;
    @Valid
    private GetAccessLevel getAccessLevel;

    public IntroInfoLevel getIntroInfoLevel() {
        return introInfoLevel;
    }

    public void setIntroInfoLevel(IntroInfoLevel introInfoLevel) {
        this.introInfoLevel = introInfoLevel;
    }

    public GetAccessLevel getGetAccessLevel() {
        return getAccessLevel;
    }

    public void setGetAccessLevel(GetAccessLevel getAccessLevel) {
        this.getAccessLevel = getAccessLevel;
    }
}
