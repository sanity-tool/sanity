static void localUndefined() {
    int i;
}

static void localLateDefinition() {
    int i;
    i = 1;
}

static void localInstaDefinition() {
    int i = 1;
}

static void addressOf() {
    int i;
    int *pi = &i;
}
