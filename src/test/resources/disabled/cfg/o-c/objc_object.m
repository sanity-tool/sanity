typedef const void * CFTypeRef;
typedef struct objc_class *Class;

struct objc_object {
    Class isa ;
};

typedef struct objc_object *id;

static id id2cf(CFTypeRef cf) {
    return ((void*)0);
}

static CFTypeRef cf2id(id X) {
    return ((void*)0);
}
