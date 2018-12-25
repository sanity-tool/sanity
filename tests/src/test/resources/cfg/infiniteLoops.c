void labelBeforeGoTo() {
start:
	goto start;
}

void conditionalBreak() {
	for (int i = 0; ; ++i) {
		if (i >= 10) {
			break;
		}
	}
}
