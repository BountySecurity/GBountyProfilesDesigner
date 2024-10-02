package gbountyprofilesdesigner.data;

import com.google.gson.annotations.SerializedName;

public final class Tuple<A, B> {
    @SerializedName("key")
    public final A left;

    @SerializedName("value")
    public final B right;

    public Tuple(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public static <A, B> Tuple<A, B> with(final A left, final B right) {
        return new Tuple<>(left, right);
    }
}
