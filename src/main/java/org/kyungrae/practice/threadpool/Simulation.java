package org.kyungrae.practice.threadpool;

import com.samsung.jio_toy.config.BandConfigurationType;
import com.samsung.jio_toy.config.SetupParameters;
import com.samsung.jio_toy.environment.EnvFactory;
import com.samsung.jio_toy.environment.EnvModel;
import com.samsung.jio_toy.environment.layout.LayoutType;
import com.samsung.jio_toy.environment.params.SpotArea;
import com.samsung.jio_toy.environment.params.UeDistributionParameters;
import com.samsung.jio_toy.environment.shadow.ShadowingType;
import com.samsung.jio_toy.main.Algorithm;
import com.samsung.jio_toy.main.AlgorithmResult;
import com.samsung.jio_toy.main.AlgorithmState;
import com.samsung.jio_toy.mrs.BasicReports;
import com.samsung.jio_toy.snapshots.AggregateDumper;
import com.samsung.jio_toy.util.RandomNumberGenerator;
import com.samsung.jio_toy.util.RandomNumberGeneratorFactory;
import com.samsung.jio_toy.util.RandomNumberGeneratorType;

public class Simulation {
    public static final int TARGET_SECTOR = 0;
    private Algorithm algorithm;
    private InputData data;
    private int carriers;

    public Simulation(InputData data) {
        this.data = data;
        this.carriers = data.getCccv().length;
    }

    public void initialize() {
        SetupParameters setupParameters = SetupParameters.builder()
                .startupRuntime(0)
                .runtime(3600)
                .randomSeed(0)
                .isd(data.getIsd())
                .envType(LayoutType.HEX_7)
                .isTrafficPatternEnabled(false)
                .uePerCell(data.getUePerCell())
                .shadowingType(ShadowingType.DISABLED)
                .bandConfigurationType(BandConfigurationType.DEFAULT_4_BANDS)
                .packetSize(data.getPacketSize())
                .build();
        RandomNumberGenerator randomNumberGenerator =
                new RandomNumberGeneratorFactory(setupParameters.getRandomSeed())
                        .of(RandomNumberGeneratorType.APACHE_MATH3);
        AggregateDumper snapshotDumper = new AggregateDumper(setupParameters, 0);
        BasicReports measurementReports = new BasicReports();
        EnvModel envModel = new EnvFactory().create(setupParameters, randomNumberGenerator);
        algorithm = Algorithm.create(snapshotDumper, measurementReports, randomNumberGenerator, envModel);
        algorithm.initialize();
        algorithm.setPacketSize(data.getPacketSize());
        algorithm.setActiveUeInterval(data.getRequestInterval());
        algorithm.setUeDistributionEnable(true);
        algorithm.enableIMLB();
        algorithm.disableFPHO();
    }

    public void updateCarrierSettings() {
        for (int carrierIndex = 0; carrierIndex < carriers; carrierIndex++) {
            int txp = data.getTxp()[carrierIndex];
            algorithm.setTxp(carrierIndex, txp);
            int penetrationLoss = data.getPenetrationLoss()[carrierIndex];
            algorithm.setPenetrationLoss(carrierIndex, penetrationLoss);
        }
    }

    public void updateImlbSettings() {
        for (int carrierIndex = 0; carrierIndex < carriers; carrierIndex++) {
            algorithm.configureCarrierIMLB(
                    carrierIndex, data.getIdleLbTh()[carrierIndex], data.getCccv()[carrierIndex]
            );
        }
    }

    public void setTargetSpotArea() {
        algorithm.setScaleOption(UeDistributionParameters.ScaleOption.SECTOR.name());
        if (algorithm.validateAndSetSpotArea(data.getAreaIndex(), data.getAreaDistance(),
                data.getAreaDegree(), data.getAreaRadius(),
                data.getBiasedUeRatio()) != SpotArea.SpotAreaValidation.VALID) {
            algorithm.setScaleOption(UeDistributionParameters.ScaleOption.NONE.toString());
        }
    }

    public Result run() {
        initialize();
        setTargetSpotArea();
        updateCarrierSettings();
        updateImlbSettings();
        Result result = new Result(carriers);
        AlgorithmResult algorithmResult = algorithm.execute();
        AlgorithmState state = algorithmResult.getState();
        double[][] rrcUe = state.getRrcUE();
        double[][] actUe = state.getActUE();
        double[][] prb = state.getCellPrb();
        double[][] vol = state.getRlcData();
        double[] tput = algorithmResult.getReward();
        int sectors = rrcUe[0].length;
        for (int i = 0; i < carriers; i++) {
            result.setPerCarrierKpi(i, rrcUe[i][TARGET_SECTOR], actUe[i][TARGET_SECTOR],
                    prb[i][TARGET_SECTOR], vol[i][TARGET_SECTOR], tput[i * sectors]);
        }
        return result;
    }
}
