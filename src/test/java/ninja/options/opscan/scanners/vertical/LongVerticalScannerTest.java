package ninja.options.opscan.scanners.vertical;

import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAPutCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static ninja.options.opscan.scanners.vertical.LongVerticalScanner.*;

class LongVerticalScannerTest {

    @Test
    void strikesFromATMCalls() {

        var baseBuilder = TDAOption.builder()
                .putCall(TDAPutCall.CALL);

        var atm = 5f;

        var firstOtmContract = baseBuilder
                .strikePrice(atm+1f)
                .isInTheMoney(false)
                .build();

        var secondOtmContract = baseBuilder
                .strikePrice(atm+2f)
                .isInTheMoney(false)
                .build();

        var firstItmContract = baseBuilder
                .isInTheMoney(true)
                .strikePrice(atm-1f)
                .build();

        var secondItmContract = baseBuilder
                .isInTheMoney(true)
                .strikePrice(atm-2f)
                .build();

        var contracts = List.of(secondItmContract, firstOtmContract,
                firstItmContract, secondOtmContract);

        assertThat(strikesFromATM(firstItmContract, contracts), equalTo(1));
        assertThat(strikesFromATM(secondItmContract, contracts), equalTo(2));
        assertThat(strikesFromATM(firstOtmContract, contracts), equalTo(1));
        assertThat(strikesFromATM(secondOtmContract, contracts), equalTo(2));

    }

    @Test
    void strikesFromATMPuts() {

        var baseBuilder = TDAOption.builder()
                .putCall(TDAPutCall.PUT);

        var atm = 5f;

        var firstOtmContract = baseBuilder
                .strikePrice(atm-1f)
                .isInTheMoney(false)
                .build();

        var secondOtmContract = baseBuilder
                .strikePrice(atm-2f)
                .isInTheMoney(false)
                .build();

        var firstItmContract = baseBuilder
                .isInTheMoney(true)
                .strikePrice(atm+1f)
                .build();

        var secondItmContract = baseBuilder
                .isInTheMoney(true)
                .strikePrice(atm+2f)
                .build();

        var contracts = List.of(secondItmContract, firstOtmContract,
                firstItmContract, secondOtmContract);

        assertThat(strikesFromATM(firstItmContract, contracts), equalTo(1));
        assertThat(strikesFromATM(secondItmContract, contracts), equalTo(2));
        assertThat(strikesFromATM(firstOtmContract, contracts), equalTo(1));
        assertThat(strikesFromATM(secondOtmContract, contracts), equalTo(2));

    }
}