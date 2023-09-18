package com.uroria.backend.impl;

import com.uroria.backend.impl.pulsar.PulsarRequestChannel;
import com.uroria.backend.impl.pulsar.PulsarUpdateChannel;
import com.uroria.backend.impl.pulsar.Result;
import com.uroria.base.io.InsaneByteArrayInputStream;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.UUID;

public abstract class AbstractManager implements RequestManager, UpdateManager {
    protected final PulsarClient pulsarClient;
    protected final Logger logger;
    protected final PulsarRequestChannel request;
    protected final PulsarUpdateChannel update;

    public AbstractManager(PulsarClient pulsarClient, Logger logger, String requestTopic, String updateTopic) {
        this.pulsarClient = pulsarClient;
        this.logger = logger;
        String identifier = UUID.randomUUID().toString();
        this.request = new PulsarRequestChannel(pulsarClient, identifier, requestTopic);
        this.update = new PulsarUpdateChannel(pulsarClient, identifier, updateTopic) {
            @Override
            public void onUpdate(InsaneByteArrayInputStream input) {
                try {
                    int target = input.readInt();
                    Object identifier = input.readObject();
                    int channel = input.readInt();
                    switch (target) {
                        case 1 -> {
                            checkObject(identifier, channel, input.readObject());
                        }
                        case 2 -> {
                            checkString(identifier, channel, input.readUTF());
                        }
                        case 3 -> {
                            checkInt(identifier, channel, input.readInt());
                        }
                        case 4 -> {
                            checkLong(identifier, channel, input.readLong());
                        }
                        case 5 -> {
                            checkBoolean(identifier, channel, input.readBoolean());
                        }
                        case 6 -> {
                            checkFloat(identifier, channel, input.readFloat());
                        }
                        case 7 -> {
                            checkDouble(identifier, channel, input.readDouble());
                        }
                    }
                } catch (Exception exception) {
                    LOGGER.error("Cannot update input", exception);
                }
            }
        };
    }

    abstract protected void start() throws PulsarClientException;

    abstract protected void shutdown() throws PulsarClientException;

    public abstract void checkObject(Object identifier, int channel, Object object);

    public abstract void checkString(Object identifier, int channel, @Nullable String value);

    public abstract void checkInt(Object identifier, int channel, int value);

    public abstract void checkLong(Object identifier, int channel, long value);

    public abstract void checkBoolean(Object identifier, int channel, boolean value);

    public abstract void checkFloat(Object identifier, int channel, float value);

    public abstract void checkDouble(Object identifier, int channel, double value);

    @Override
    public final void updateObject(Serializable identifier, int channel, Object object) {
        this.update.update(out -> {
            try {
                out.writeInt(1);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeObject(object);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Object " + channel, exception);
            }
        });
    }

    @Override
    public final void updateString(Serializable identifier, int channel, String string) {
        this.update.update(out -> {
            try {
                out.writeInt(2);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeUTF(string);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update String " + channel, exception);
            }
        });
    }

    @Override
    public final void updateInt(Serializable identifier, int channel, int i) {
        this.update.update(out -> {
            try {
                out.writeInt(3);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeInt(i);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Int " + channel, exception);
            }
        });
    }

    @Override
    public final void updateLong(Serializable identifier, int channel, long l) {
        this.update.update(out -> {
            try {
                out.writeInt(4);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeLong(l);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Long " + channel, exception);
            }
        });
    }

    @Override
    public final void updateBoolean(Serializable identifier, int channel, boolean bool) {
        this.update.update(out -> {
            try {
                out.writeInt(5);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeBoolean(bool);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Boolean " + channel, exception);
            }
        });
    }

    @Override
    public final void updateFloat(Serializable identifier, int channel, float f) {
        this.update.update(out -> {
            try {
                out.writeInt(6);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeFloat(f);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Float " + channel, exception);
            }
        });
    }

    @Override
    public final void updateDouble(Serializable identifier, int channel, double d) {
        this.update.update(out -> {
            try {
                out.writeInt(7);
                out.writeObject(identifier);
                out.writeInt(channel);
                out.writeDouble(d);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot update Double " + channel, exception);
            }
        });
    }

    @Override
    public final Object getObject(Serializable identifier, int channel, Object defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(1);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Object for " + channel, error.getError());
            return null;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readObject();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of Object response for " + channel, exception);
            return null;
        }
    }

    @Override
    public final String getString(Serializable identifier, int channel, String defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(2);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request String for " + channel, error.getError());
            return null;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readUTF();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of String response for " + channel, exception);
            return null;
        }
    }

    @Override
    public final int getInt(Serializable identifier, int channel, int defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(3);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Int for " + channel, error.getError());
            return defVal;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readInt();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of Int response for " + channel, exception);
            return defVal;
        }
    }

    @Override
    public final long getLong(Serializable identifier, int channel, int defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(4);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Long for " + channel, error.getError());
            return defVal;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readLong();
        } catch (Exception exception) {
            this.logger.error("Cannot read Long of Object response for " + channel, exception);
            return defVal;
        }
    }

    @Override
    public final boolean getBoolean(Serializable identifier, int channel, boolean defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(5);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Boolean for " + channel, error.getError());
            return defVal;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readBoolean();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of Boolean response for " + channel, exception);
            return defVal;
        }
    }

    @Override
    public final float getFloat(Serializable identifier, int channel, float defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(6);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Float for " + channel, error.getError());
            return defVal;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readFloat();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of Float response for " + channel, exception);
            return defVal;
        }
    }

    @Override
    public final double getDouble(Serializable identifier, int channel, double defVal) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(7);
                out.writeInt(channel);
                out.writeObject(identifier);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request Double for " + channel, error.getError());
            return defVal;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return defVal;
            return in.readDouble();
        } catch (Exception exception) {
            this.logger.error("Cannot read object of Double response for " + channel, exception);
            return defVal;
        }
    }
}
