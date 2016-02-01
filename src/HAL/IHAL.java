package HAL;

import com.google.common.base.Optional;

public interface IHAL {

	void printOnDisplay(String text, Optional<Long> waitDuration);

}
