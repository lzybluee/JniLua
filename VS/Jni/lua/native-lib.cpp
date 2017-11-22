#include <jni.h>
#include <string>
#include <iostream>
#include <sstream>

using namespace std;

extern "C" {
#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"
}

static stringstream stream;
static lua_State *state;
static bool error;

LUALIB_API int lua_print(lua_State *L) {
    const char *s = lua_tostring(L, -1);
    printf("%s\n", s);
    stream << s << endl;
    return 0;
}

static void loadFile(const char* path) {
    FILE* file = fopen(path, "r");
    if(file) {
        fclose(file);
    } else {
        stream << "Warning: file not found. " << path << endl;
        return;
    }

    if (luaL_loadfile(state, path) || lua_pcall(state, 0, 0, 0)) {
        error = true;
        const char *e = lua_tostring(state, -1);
		printf("Error : %s %s\n", path, e);
        stream << "Error: " << path << " " << e << endl;
    }
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_luaPushString(JNIEnv *env, jclass cls, jstring k, jstring v) {
    const char *key = env->GetStringUTFChars(k, 0);
    if (v) {
        const char *value = env->GetStringUTFChars(v, 0);
        lua_pushstring(state, value);
        env->ReleaseStringUTFChars(v, value);
    }
    else {
        lua_pushnil(state);
    }
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_luaPushInteger(JNIEnv *env, jclass cls, jstring k, jint v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushinteger(state, (int)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_luaPushBoolean(JNIEnv *env, jclass cls, jstring k, jboolean v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushboolean(state, (bool)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_luaPushFloat(JNIEnv *env, jclass cls, jstring k, jfloat v)
{
    const char *key = env->GetStringUTFChars(k, 0);
    lua_pushnumber(state, (float)v);
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_luaPushStringArray(JNIEnv *env, jclass cls, jstring k, jobjectArray v) {
    const char *key = env->GetStringUTFChars(k, 0);
    lua_newtable(state);
    int count = env->GetArrayLength(v);
    for (int i = 0; i < count; i++) {
        lua_pushinteger(state, i + 1);
        jstring jstr = (jstring) (env->GetObjectArrayElement(v, i));
        const char* cstr = env->GetStringUTFChars(jstr, 0);
        lua_pushstring(state, cstr);
        env->ReleaseStringUTFChars(jstr, cstr);
        lua_settable(state, -3);
    }
    lua_setglobal(state, key);
    env->ReleaseStringUTFChars(k, key);
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_initLua(JNIEnv *env, jclass cls, jstring jfile) {
    state = luaL_newstate();
    luaL_openlibs(state);
    lua_pushcfunction(state, lua_print);
    lua_setglobal(state, "print");

    const char *file = env->GetStringUTFChars(jfile, 0);
    loadFile(file);
    env->ReleaseStringUTFChars(jfile, file);
}

extern "C"
JNIEXPORT jint Java_lu_cifer_mtgviewer_LuaScript_getResult(JNIEnv *env, jclass cls) {
    if (error)
        return 2;

    lua_getglobal(state, "result");
    bool result = (bool)lua_toboolean(state, -1);
    return result ? 1 : 0;
}

extern "C"
JNIEXPORT jstring Java_lu_cifer_mtgviewer_LuaScript_runScript(JNIEnv *env, jclass cls, jstring jcode, jstring jfile) {
    const char *code = env->GetStringUTFChars(jcode, 0);
    const char *file = env->GetStringUTFChars(jfile, 0);

    stream.str("");

    loadFile(file);

    if (luaL_loadstring(state, code) || lua_pcall(state, 0, 0, 0))
    {
        error = true;
        const char *e = lua_tostring(state, -1);
		printf("Error : %s\n", e);
        stream << "Error: " << e;
    } else {
        error = false;
        stream << "Finished";
    }

    env->ReleaseStringUTFChars(jcode, code);
    env->ReleaseStringUTFChars(jfile, file);

    std::string ret = stream.str();
    return env->NewStringUTF(ret.c_str());
}

extern "C"
JNIEXPORT void Java_lu_cifer_mtgviewer_LuaScript_closeLua(JNIEnv *env, jclass cls) {
    lua_close(state);
}
