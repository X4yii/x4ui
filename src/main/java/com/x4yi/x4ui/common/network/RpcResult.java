package com.x4yi.x4ui.common.network;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public final class RpcResult {
    private final boolean ok;
    private final String errorCode;
    private final String errorMessage;
    private final NBTTagCompound data;

    private RpcResult(boolean ok, String errorCode, String errorMessage, @Nullable NBTTagCompound data) {
        this.ok = ok;
        this.errorCode = errorCode != null ? errorCode : "";
        this.errorMessage = errorMessage != null ? errorMessage : "";
        this.data = data != null ? data : new NBTTagCompound();
    }

    public static RpcResult ok() {
        return new RpcResult(true, "", "", new NBTTagCompound());
    }

    public static RpcResult ok(@Nullable NBTTagCompound data) {
        return new RpcResult(true, "", "", data);
    }

    public static RpcResult error(String code, String message) {
        return new RpcResult(false, code, message, null);
    }

    public boolean isOk() {
        return ok;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public NBTTagCompound getData() {
        return data;
    }
}

