#include <jni.h>
#include <pty.h>
#include <string>
#include <unistd.h>
#include <asm-generic/fcntl.h>

extern "C" {

int throw_runtime_exception(JNIEnv *env, const std::string& message) {
    jclass className = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(className, message.c_str());
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_example_terminal_JNI_createPty(
        JNIEnv *env, jclass clazz) {
    int master;
    int pid = forkpty(&master, nullptr, nullptr, nullptr);
    if (pid == 0) { // slave
        execvp("sh", nullptr);
    } else if (pid > 0) { // master
        return master;
    } else {
        return throw_runtime_exception(env, "Failed to forkpty");
    }

    fcntl(master, F_SETFL, fcntl(master, F_GETFL) | O_NONBLOCK);
    return master;
}

JNIEXPORT void JNICALL Java_com_example_terminal_JNI_close(
        JNIEnv *env, jclass clazz, jint fd) {
    close(fd);
}

} // extern "C"