package platform.client.ui.element;

public final class DragSpring {
    private static final float FREQUENCY = 4.0f;
    private static final float DAMPING_RATIO = 0.85f;
    private static final float OMEGA = (float) (Math.PI * 2.0d * FREQUENCY);
    private static final float STIFFNESS = OMEGA * OMEGA;
    private static final float DAMPING = 2.0f * DAMPING_RATIO * OMEGA;
    private static final float STEP = 1.0f / 240.0f;
    private static final float MAX_STEP = 0.1f;
    private static final float SETTLE_POSITION = 0.01f;
    private static final float SETTLE_VELOCITY = 0.01f;

    private float value;
    private float target;
    private float velocity;
    private long lastTime = Long.MIN_VALUE;

    public DragSpring(float initial) {
        this.value = initial;
        this.target = initial;
    }

    public void a(float target) {
        this.target = target;
    }

    public void b(float value) {
        this.value = value;
        this.target = value;
        this.velocity = 0.0f;
    }

    public float a() {
        return this.value;
    }

    public boolean b() {
        return Math.abs(this.target - this.value) > SETTLE_POSITION || Math.abs(this.velocity) > SETTLE_VELOCITY;
    }

    public float c(long now) {
        if (this.lastTime == Long.MIN_VALUE) {
            this.lastTime = now;
            this.value = this.target;
            this.velocity = 0.0f;
            return this.value;
        }
        float dt = (now - this.lastTime) / 1000.0f;
        this.lastTime = now;
        if (dt <= 0.0f) {
            return this.value;
        }
        if (dt > MAX_STEP) {
            dt = MAX_STEP;
        }
        float remaining = dt;
        int guard = 0;
        while (remaining > 0.0f && guard < 96) {
            float step = Math.min(remaining, STEP);
            float acceleration = (this.target - this.value) * STIFFNESS - this.velocity * DAMPING;
            this.velocity += acceleration * step;
            this.value += this.velocity * step;
            remaining -= step;
            guard++;
            if (!b()) {
                this.value = this.target;
                this.velocity = 0.0f;
                break;
            }
        }
        if (Float.isNaN(this.value) || Float.isInfinite(this.value)) {
            this.value = this.target;
            this.velocity = 0.0f;
        }
        return this.value;
    }
}



